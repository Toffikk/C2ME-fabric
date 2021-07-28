package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadly.concurrent.UnfairExecutor;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;

public class BiomeCache {

    public static final UnfairExecutor EXECUTOR = new UnfairExecutor(2, new ThreadFactoryBuilder().setNameFormat("C2ME biomes #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build());
    private static final Logger LOGGER = LogManager.getLogger("C2ME Biome Cache");

    private final Registry<Biome> registry;

    private final UncachedBiomeSource uncachedBiomeSource;

    private final AtomicReference<LevelHeightAccessor> finalHeightLimitView = new AtomicReference<>(null);

    public BiomeCache(BiomeProvider sampler, Registry<Biome> registry, List<Biome> biomes) {
        this.registry = registry;
        this.uncachedBiomeSource = new UncachedBiomeSource(biomes, sampler, registry);
    }

    private final LoadingCache<ChunkPos, ChunkBiomeContainer> biomeCache = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(8192)
            .build(new CacheLoader<>() {
                @Override
                public ChunkBiomeContainer load(ChunkPos key) {
                    if (finalHeightLimitView.get() == null) throw new IllegalStateException(String.format("Cannot populate non-configured biome cache %s", BiomeCache.this));
                    return new ChunkBiomeContainer(registry, finalHeightLimitView.get(), key, uncachedBiomeSource);
                }
            });

    private final ThreadLocal<WeakHashMap<ChunkPos, ChunkBiomeContainer>> threadLocalCache = ThreadLocal.withInitial(WeakHashMap::new);

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ, boolean fast) {
        if (finalHeightLimitView.get() == null) {
            return uncachedBiomeSource.getNoiseBiome(biomeX, biomeY, biomeZ);
        }
        final ChunkPos chunkPos = new ChunkPos(QuartPos.toSection(biomeX), QuartPos.toSection(biomeZ));
        final int startX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        final int startZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        if (fast) {
            final WeakHashMap<ChunkPos, ChunkBiomeContainer> localCache = threadLocalCache.get();
            final ChunkBiomeContainer biomeArray1 = localCache.get(chunkPos);
            if (biomeArray1 != null) return biomeArray1.getNoiseBiome(biomeX - startX, biomeY, biomeZ - startZ);
            final ChunkBiomeContainer biomeArray2 = this.biomeCache.asMap().get(chunkPos);
            if (biomeArray2 != null) {
                localCache.put(chunkPos, biomeArray2);
                return biomeArray2.getNoiseBiome(biomeX - startX, biomeY, biomeZ - startZ);
            }
            return uncachedBiomeSource.getNoiseBiome(biomeX, biomeY, biomeZ);
        } else {
            return threadLocalCache.get().computeIfAbsent(chunkPos, biomeCache).getNoiseBiome(biomeX - startX, biomeY, biomeZ - startZ);
        }
    }

    public ChunkBiomeContainer preloadBiomes(LevelHeightAccessor view, ChunkPos pos, ChunkBiomeContainer def) {
        Preconditions.checkNotNull(view);
        if (!finalHeightLimitView.compareAndSet(null, view)) {
            if (view.getMinBuildHeight() != finalHeightLimitView.get().getMinBuildHeight()
                    || view.getMaxBuildHeight() != finalHeightLimitView.get().getMaxBuildHeight())
                throw new IllegalArgumentException(String.format("Cannot modify %s height value : expected %d ~ %d but got %d ~ %d",
                        this, finalHeightLimitView.get().getMinBuildHeight(), finalHeightLimitView.get().getMaxBuildHeight(), view.getMinBuildHeight(), view.getMaxBuildHeight()));
        } else {
            LOGGER.info("Successfully setup {} with height: {} ~ {}", this, view.getMinBuildHeight(), view.getMaxBuildHeight());
        }
        if (def != null) {
            this.biomeCache.put(pos, def);
            return def;
        } else {
            return this.biomeCache.getUnchecked(pos);
        }
    }

    public interface BiomeProvider {
        Biome sample(Registry<Biome> biomeRegistry, int x, int y, int z);
    }

}
