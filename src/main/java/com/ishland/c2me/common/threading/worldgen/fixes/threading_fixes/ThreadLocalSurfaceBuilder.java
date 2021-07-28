package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;

public class ThreadLocalSurfaceBuilder<C extends SurfaceBuilderConfiguration> extends SurfaceBuilder<C> {

    private final ThreadLocal<SurfaceBuilder<C>> surfaceBuilderThreadLocal;

    public ThreadLocalSurfaceBuilder(Supplier<SurfaceBuilder<C>> supplier, Codec<C> codec) {
        super(codec);
        this.surfaceBuilderThreadLocal = ThreadLocal.withInitial(supplier);
    }

    @Override
    public void initNoise(long seed) {
        this.surfaceBuilderThreadLocal.get().initNoise(seed);
    }

    @Override
    public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, C surfaceConfig) {
        this.surfaceBuilderThreadLocal.get().apply(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, i, l, surfaceConfig);
    }
}
