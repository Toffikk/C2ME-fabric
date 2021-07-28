package com.ishland.c2me.tests.worlddiff;

import com.google.common.collect.ImmutableSet;
import com.ibm.asyncutil.locks.AsyncSemaphore;
import com.ibm.asyncutil.locks.FairAsyncSemaphore;
import com.ishland.c2me.tests.worlddiff.mixin.IIOWorker;
import com.ishland.c2me.tests.worlddiff.mixin.IWorldUpgrader;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ComparisonSession implements Closeable {

    private final WorldHandle baseWorld;
    private final WorldHandle targetWorld;

    public ComparisonSession(File from, File to) {
        try {
            baseWorld = getWorldHandle(from, "Base world");
            targetWorld = getWorldHandle(to, "Target world");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void compareChunks() {
        System.out.println("Starting comparison of chunks");
        AsyncSemaphore working = new FairAsyncSemaphore(Runtime.getRuntime().availableProcessors() * 4);
        final HashSet<ResourceKey<Level>> worlds = new HashSet<>(baseWorld.chunkPosesMap.keySet());
        worlds.retainAll(targetWorld.chunkPosesMap.keySet());
        for (ResourceKey<Level> world : worlds) {
            System.out.printf("Filtering chunks in world %s\n", world);
            final HashSet<ChunkPos> chunks = new HashSet<>(baseWorld.chunkPosesMap.get(world));
            chunks.retainAll(targetWorld.chunkPosesMap.get(world));
            final int totalChunks = chunks.size();
            final IOWorker regionBaseIo = baseWorld.regionIoWorkers.get(world);
            final IOWorker regionTargetIo = targetWorld.regionIoWorkers.get(world);
            AtomicLong completedChunks = new AtomicLong();
            AtomicLong completedBlocks = new AtomicLong();
            AtomicLong differenceBlocks = new AtomicLong();
            ConcurrentHashMap<ResourceLocation, AtomicLong> blockDifference = new ConcurrentHashMap<>();
            final CompletableFuture<Void> future = CompletableFuture.allOf(chunks.stream().map(pos -> working.acquire().toCompletableFuture().thenCompose(unused ->
                    ((IIOWorker) regionBaseIo).invokeLoadAsync(pos)
                            .thenCombineAsync(((IIOWorker) regionTargetIo).invokeLoadAsync(pos), (chunkDataBase, chunkDataTarget) -> {
                                try {
                                    if (ChunkSerializer.getChunkTypeFromTag(chunkDataBase) == ChunkStatus.ChunkType.PROTOCHUNK
                                            || ChunkSerializer.getChunkTypeFromTag(chunkDataBase) == ChunkStatus.ChunkType.PROTOCHUNK)
                                        return null;
                                    final Map<SectionPos, LevelChunkSection> sectionsBase = readSections(pos, chunkDataBase);
                                    final Map<SectionPos, LevelChunkSection> sectionsTarget = readSections(pos, chunkDataTarget);
                                    sectionsBase.forEach((chunkSectionPos, chunkSectionBase) -> {
                                        final LevelChunkSection chunkSectionTarget = sectionsTarget.get(chunkSectionPos);
                                        if (chunkSectionBase == null || chunkSectionTarget == null) {
                                            completedBlocks.addAndGet(16 * 16 * 16);
                                            differenceBlocks.addAndGet(16 * 16 * 16);
                                            return;
                                        }
                                        for (int x = 0; x < 16; x++)
                                            for (int y = 0; y < 16; y++)
                                                for (int z = 0; z < 16; z++) {
                                                    final BlockState state1 = chunkSectionBase.getBlockState(x, y, z);
                                                    final BlockState state2 = chunkSectionTarget.getBlockState(x, y, z);
                                                    if (!blockStateEquals(state1, state2)) {
                                                        differenceBlocks.incrementAndGet();
                                                        if (!Registry.BLOCK.getKey(state1.getBlock()).equals(Registry.BLOCK.getKey(state2.getBlock()))) {
                                                            blockDifference.computeIfAbsent(Registry.BLOCK.getKey(state1.getBlock()), unused1 -> new AtomicLong()).incrementAndGet();
                                                            blockDifference.computeIfAbsent(Registry.BLOCK.getKey(state2.getBlock()), unused1 -> new AtomicLong()).incrementAndGet();
                                                        }
                                                    }
                                                    completedBlocks.incrementAndGet();
                                                }
                                    });
                                    return null;
                                } catch (Throwable t) {
                                    t.printStackTrace(System.err);
                                    throw new RuntimeException(t);
                                } finally {
                                    completedChunks.incrementAndGet();
                                    working.release();
                                }
                            })
            )).distinct().toArray(CompletableFuture[]::new));
            while (!future.isDone()) {
                System.out.printf("[noprint]%s: %d / %d (%.1f%%) chunks, %d blocks, %d block differences (%.4f%%)\n",
                        world, completedChunks.get(), totalChunks, completedChunks.get() / (float) totalChunks * 100.0,
                        completedBlocks.get(), differenceBlocks.get(), differenceBlocks.get() / completedBlocks.floatValue() * 100.0);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
            System.err.printf("Comparison completed for %s: block state differences: %d / %d (%.4f%%)\n",
                    world, differenceBlocks.get(), completedBlocks.get(), differenceBlocks.get() / completedBlocks.floatValue() * 100.0);
            System.err.print(blockDifference + "\n");
        }

    }

    @Override
    public void close() throws IOException {
        this.baseWorld.handle.close();
        this.targetWorld.handle.close();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean blockStateEquals(BlockState state1, BlockState state2) {
        if (!Registry.BLOCK.getKey(state1.getBlock()).equals(Registry.BLOCK.getKey(state2.getBlock()))) return false;
        for (Property property : state1.getProperties()) {
            if (state1.getValue(property).compareTo(state2.getValue(property)) != 0) return false;
        }
        return true;
    }

    private static Map<SectionPos, LevelChunkSection> readSections(ChunkPos pos, CompoundTag chunkData) {
        ListTag nbtList = chunkData.getCompound("Level").getList("Sections", 10);
        HashMap<SectionPos, LevelChunkSection> result = new HashMap<>();
        for (int i = 0; i < nbtList.size(); i++) {
            final CompoundTag sectionData = nbtList.getCompound(i);
            int y = sectionData.getByte("Y");
            if (sectionData.contains("Palette", 9) && sectionData.contains("BlockStates", 12)) {
                final LevelChunkSection chunkSection = new LevelChunkSection(y);
                chunkSection.getStates().read(sectionData.getList("Palette", 10), sectionData.getLongArray("BlockStates"));
                chunkSection.recalcBlockCounts();
                result.put(SectionPos.of(pos, y), chunkSection);
            }
        }
        return result;
    }

    private static WorldHandle getWorldHandle(File worldFolder, String description) throws IOException {
        final LevelStorageSource levelStorage = LevelStorageSource.createDefault(worldFolder.toPath());
        final LevelStorageSource.LevelStorageAccess session = levelStorage.createAccess(worldFolder.getAbsolutePath());

        System.out.printf("Reading world data for %s\n", description);
        RegistryAccess.RegistryHolder impl = RegistryAccess.builtin();
        DataPackConfig dataPackSettings = session.getDataPacks();
        PackRepository resourcePackManager = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(session.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
        DataPackConfig dataPackSettings2 = MinecraftServer.configurePackRepository(resourcePackManager, dataPackSettings == null ? DataPackConfig.DEFAULT : dataPackSettings, false);
        ServerResources serverResourceManager2;
        try {
            serverResourceManager2 = ServerResources.loadResources(resourcePackManager.openAllSelected(), impl, Commands.CommandSelection.DEDICATED, 2, Util.backgroundExecutor(), Runnable::run).get();
        } catch (Throwable t) {
            resourcePackManager.close();
            throw new RuntimeException("Cannot load data packs", t);
        }
        final RegistryReadOps<Tag> dynamicOps = RegistryReadOps.createAndLoad(NbtOps.INSTANCE, serverResourceManager2.getResourceManager(), impl);
        WorldData saveProperties = session.getDataTag(dynamicOps, dataPackSettings2);
        if (saveProperties == null) {
            resourcePackManager.close();
            throw new FileNotFoundException();
        }
        final ImmutableSet<ResourceKey<Level>> worldKeys = saveProperties.worldGenSettings().levels();
        final WorldUpgrader worldUpgrader = new WorldUpgrader(session, DataFixers.getDataFixer(), worldKeys, false);
        final HashMap<ResourceKey<Level>, List<ChunkPos>> chunkPosesMap = new HashMap<>();
        for (ResourceKey<Level> world : worldKeys) {
            System.out.printf("%s: Counting chunks for world %s\n", description, world);
            //noinspection ConstantConditions
            chunkPosesMap.put(world, ((IWorldUpgrader) worldUpgrader).invokeGetAllChunkPos(world));
        }
        final HashMap<ResourceKey<Level>, IOWorker> regionIoWorkers = new HashMap<>();
        final HashMap<ResourceKey<Level>, IOWorker> poiIoWorkers = new HashMap<>();
        for (ResourceKey<Level> world : worldKeys) {
            regionIoWorkers.put(world, new IOWorker(new File(session.getDimensionPath(world), "region"), true, "chunk") {
            });
            poiIoWorkers.put(world, new IOWorker(new File(session.getDimensionPath(world), "poi"), true, "poi") {
            });
        }
        return new WorldHandle(chunkPosesMap, regionIoWorkers, poiIoWorkers, () -> {
            System.out.println("Shutting down IOWorkers...");
            Stream.concat(regionIoWorkers.values().stream(), poiIoWorkers.values().stream()).forEach(ioWorker -> {
                ioWorker.synchronize(true).join();
                try {
                    ioWorker.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Closing world");
            resourcePackManager.close();
            session.close();
            System.out.println("World closed");
        });
    }

    public record WorldHandle(HashMap<ResourceKey<Level>, List<ChunkPos>> chunkPosesMap,
                              HashMap<ResourceKey<Level>, IOWorker> regionIoWorkers,
                              HashMap<ResourceKey<Level>, IOWorker> poiIoWorkers,
                              Closeable handle) {
    }

}
