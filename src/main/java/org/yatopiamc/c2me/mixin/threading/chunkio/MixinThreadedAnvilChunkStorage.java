package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.AsyncSerializationManager;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import org.yatopiamc.c2me.common.threading.chunkio.IAsyncChunkStorage;
import org.yatopiamc.c2me.common.threading.chunkio.ISerializingRegionBasedStorage;
import org.yatopiamc.c2me.common.util.SneakyThrow;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;

@Mixin(ChunkMap.class)
public abstract class MixinThreadedAnvilChunkStorage extends ChunkStorage implements ChunkHolder.PlayerProvider {

    public MixinThreadedAnvilChunkStorage(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    @Shadow
    @Final
    private ServerLevel world;

    @Shadow
    @Final
    private StructureManager structureManager;

    @Shadow
    @Final
    private PoiManager pointOfInterestStorage;

    @Shadow
    protected abstract byte method_27053(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void method_27054(ChunkPos chunkPos);

    @Shadow
    @Final
    private Supplier<DimensionDataStorage> persistentStateManagerFactory;

    @Shadow
    @Final
    private BlockableEventLoop<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract boolean method_27055(ChunkPos chunkPos);

    private AsyncNamedLock<ChunkPos> chunkLock = AsyncNamedLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        chunkLock = AsyncNamedLock.createFair();
    }

    private Set<ChunkPos> scheduledChunks = new HashSet<>();

    /**
     * @author ishland
     * @reason async io and deserialization
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> loadChunk(ChunkPos pos) {
        if (scheduledChunks == null) scheduledChunks = new HashSet<>();
        synchronized (scheduledChunks) {
            if (scheduledChunks.contains(pos)) throw new IllegalArgumentException("Already scheduled");
            scheduledChunks.add(pos);
        }

        final CompletableFuture<CompoundTag> poiData = ((IAsyncChunkStorage) this.pointOfInterestStorage.worker).getNbtAtAsync(pos);

        final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = getUpdatedChunkNbtAtAsync(pos).thenApplyAsync(compoundTag -> {
            if (compoundTag != null) {
                try {
                    if (compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8)) {
                        return ChunkSerializer.read(this.world, this.structureManager, this.pointOfInterestStorage, pos, compoundTag);
                    }

                    LOGGER.warn("Chunk file at {} is missing level data, skipping", pos);
                } catch (Throwable t) {
                    LOGGER.error("Couldn't load chunk {}, chunk data will be lost!", pos, t);
                }
            }
            return null;
        }, ChunkIoThreadingExecutorUtils.serializerExecutor).thenCombine(poiData, (protoChunk, tag) -> protoChunk).thenApplyAsync(protoChunk -> {
            ((ISerializingRegionBasedStorage) this.pointOfInterestStorage).update(pos, poiData.join());
            ChunkIoMainThreadTaskUtils.drainQueue();
            if (protoChunk != null) {
                this.method_27053(pos, protoChunk.getStatus().getChunkType());
                return Either.left(protoChunk);
            } else {
                this.method_27054(pos);
                return Either.left(new ProtoChunk(pos, UpgradeData.EMPTY, this.world));
            }
        }, this.mainThreadExecutor);
        future.exceptionally(throwable -> null).thenRun(() -> {
            synchronized (scheduledChunks) {
                scheduledChunks.remove(pos);
            }
        });
        return future;

        // [VanillaCopy] - for reference
        /*
        return CompletableFuture.supplyAsync(() -> {
         try {
            this.world.getProfiler().visit("chunkLoad");
            CompoundTag compoundTag = this.getUpdatedChunkNbt(pos);
            if (compoundTag != null) {
               boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
               if (bl) {
                  Chunk chunk = ChunkSerializer.deserialize(this.world, this.structureManager, this.pointOfInterestStorage, pos, compoundTag);
                  this.method_27053(pos, chunk.getStatus().getChunkType());
                  return Either.left(chunk);
               }

               LOGGER.error((String)"Chunk file at {} is missing level data, skipping", (Object)pos);
            }
         } catch (CrashException var5) {
            Throwable throwable = var5.getCause();
            if (!(throwable instanceof IOException)) {
               this.method_27054(pos);
               throw var5;
            }

            LOGGER.error((String)"Couldn't load chunk {}", (Object)pos, (Object)throwable);
         } catch (Exception var6) {
            LOGGER.error((String)"Couldn't load chunk {}", (Object)pos, (Object)var6);
         }

         this.method_27054(pos);
         return Either.left(new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA, this.world));
      }, this.mainThreadExecutor);
         */
    }

    private CompletableFuture<CompoundTag> getUpdatedChunkNbtAtAsync(ChunkPos pos) {
        return chunkLock.acquireLock(pos).toCompletableFuture().thenCompose(lockToken -> ((IAsyncChunkStorage) this.worker).getNbtAtAsync(pos).thenApply(compoundTag -> {
            if (compoundTag != null)
                return this.upgradeChunkTag(this.world.dimension(), this.persistentStateManagerFactory, compoundTag);
            else return null;
        }).handle((tag, throwable) -> {
            lockToken.releaseLock();
            if (throwable != null)
                SneakyThrow.sneaky(throwable);
            return tag;
        }));
    }

    private ConcurrentLinkedQueue<CompletableFuture<Void>> saveFutures = new ConcurrentLinkedQueue<>();

    @Dynamic
    @Redirect(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;save(Lnet/minecraft/world/chunk/Chunk;)Z")) // method: consumer in tryUnloadChunk
    private boolean asyncSave(ChunkMap tacs, ChunkAccess chunk) {
        // TODO [VanillaCopy] - check when updating minecraft version
        this.pointOfInterestStorage.flush(chunk.getPos());
        if (!chunk.isUnsaved()) {
            return false;
        } else {
            chunk.setUnsaved(false);
            ChunkPos chunkPos = chunk.getPos();

            try {
                ChunkStatus chunkStatus = chunk.getStatus();
                if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                    if (this.method_27055(chunkPos)) {
                        return false;
                    }

                    if (chunkStatus == ChunkStatus.EMPTY && chunk.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                        return false;
                    }
                }

                this.world.getProfiler().incrementCounter("chunkSave");
                // C2ME start - async serialization
                if (saveFutures == null) saveFutures = new ConcurrentLinkedQueue<>();
                AsyncSerializationManager.Scope scope = new AsyncSerializationManager.Scope(chunk, world);

                saveFutures.add(chunkLock.acquireLock(chunk.getPos()).toCompletableFuture().thenCompose(lockToken ->
                        CompletableFuture.supplyAsync(() -> {
                            scope.open();
                            AsyncSerializationManager.push(scope);
                            try {
                                return ChunkSerializer.write(this.world, chunk);
                            } finally {
                                AsyncSerializationManager.pop(scope);
                            }
                        }, ChunkIoThreadingExecutorUtils.serializerExecutor)
                                .thenAcceptAsync(compoundTag -> this.write(chunkPos, compoundTag), this.mainThreadExecutor)
                                .handle((unused, throwable) -> {
                            lockToken.releaseLock();
                            if (throwable != null)
                                LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, throwable);
                            return unused;
                        })));
                this.method_27053(chunkPos, chunkStatus.getChunkType());
                // C2ME end
                return true;
            } catch (Exception var5) {
                LOGGER.error((String) "Failed to save chunk {},{}", (Object) chunkPos.x, chunkPos.z, var5);
                return false;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        ChunkIoThreadingExecutorUtils.serializerExecutor.execute(() -> saveFutures.removeIf(CompletableFuture::isDone));
    }

    @Override
    public void flushWorker() {
        final CompletableFuture<Void> future = CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]));
        this.mainThreadExecutor.managedBlock(future::isDone); // wait for serialization to complete
        super.flushWorker();
    }
}
