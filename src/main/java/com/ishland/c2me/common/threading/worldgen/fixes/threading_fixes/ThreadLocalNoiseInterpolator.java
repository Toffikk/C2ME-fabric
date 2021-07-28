package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseInterpolator;

public class ThreadLocalNoiseInterpolator extends NoiseInterpolator {

    private final ThreadLocal<NoiseInterpolator> noiseInterpolatorThreadLocal;

    public ThreadLocalNoiseInterpolator(int sizeX, int sizeY, int sizeZ, ChunkPos pos, int minY, NoiseColumnFiller columnSampler) {
        super(sizeX, sizeY, sizeZ, pos, minY, columnSampler);
        this.noiseInterpolatorThreadLocal = ThreadLocal.withInitial(() -> new NoiseInterpolator(sizeX, sizeY, sizeZ, pos, minY, columnSampler));
    }

    @Override
    public void initializeForFirstCellX() {
        this.noiseInterpolatorThreadLocal.get().initializeForFirstCellX();
    }

    @Override
    public void advanceCellX(int x) {
        this.noiseInterpolatorThreadLocal.get().advanceCellX(x);
    }

    @Override
    public void selectCellYZ(int noiseY, int noiseZ) {
        this.noiseInterpolatorThreadLocal.get().selectCellYZ(noiseY, noiseZ);
    }

    @Override
    public void updateForY(double deltaY) {
        this.noiseInterpolatorThreadLocal.get().updateForY(deltaY);
    }

    @Override
    public void updateForX(double deltaX) {
        this.noiseInterpolatorThreadLocal.get().updateForX(deltaX);
    }

    @Override
    public double calculateValue(double deltaZ) {
        return this.noiseInterpolatorThreadLocal.get().calculateValue(deltaZ);
    }

    @Override
    public void swapSlices() {
        this.noiseInterpolatorThreadLocal.get().swapSlices();
    }
}
