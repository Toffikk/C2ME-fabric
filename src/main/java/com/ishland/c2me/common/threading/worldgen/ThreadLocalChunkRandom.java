package com.ishland.c2me.common.threading.worldgen;

import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class ThreadLocalChunkRandom extends WorldgenRandom {

    private final ThreadLocal<WorldgenRandom> chunkRandomThreadLocal;

    public ThreadLocalChunkRandom(long seed) {
        this(seed, chunkRandom -> {});
    }

    public ThreadLocalChunkRandom(long seed, Consumer<WorldgenRandom> preHook) {
        this.chunkRandomThreadLocal = ThreadLocal.withInitial(() -> {
            final WorldgenRandom chunkRandom = new WorldgenRandom(seed);
            preHook.accept(chunkRandom);
            return chunkRandom;
        });
    }

    @Override
    public int getCount() {
        return chunkRandomThreadLocal.get().getCount();
    }

    @Override
    public int next(int count) {
        return chunkRandomThreadLocal.get().next(count);
    }

    @Override
    public long setBaseChunkSeed(int chunkX, int chunkZ) {
        return chunkRandomThreadLocal.get().setBaseChunkSeed(chunkX, chunkZ);
    }

    @Override
    public long setDecorationSeed(long worldSeed, int blockX, int blockZ) {
        return chunkRandomThreadLocal.get().setDecorationSeed(worldSeed, blockX, blockZ);
    }

    @Override
    public long setFeatureSeed(long populationSeed, int index, int step) {
        return chunkRandomThreadLocal.get().setFeatureSeed(populationSeed, index, step);
    }

    @Override
    public long setLargeFeatureSeed(long worldSeed, int chunkX, int chunkZ) {
        return chunkRandomThreadLocal.get().setLargeFeatureSeed(worldSeed, chunkX, chunkZ);
    }

    @Override
    public long setBaseStoneSeed(long worldSeed, int x, int y, int z) {
        return chunkRandomThreadLocal.get().setBaseStoneSeed(worldSeed, x, y, z);
    }

    @Override
    public long setLargeFeatureWithSalt(long worldSeed, int regionX, int regionZ, int salt) {
        return chunkRandomThreadLocal.get().setLargeFeatureWithSalt(worldSeed, regionX, regionZ, salt);
    }

    @Override
    public synchronized void setSeed(long seed) {
        if (chunkRandomThreadLocal == null) return; // Special case when doing <init>
        chunkRandomThreadLocal.get().setSeed(seed);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        chunkRandomThreadLocal.get().nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return chunkRandomThreadLocal.get().nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return chunkRandomThreadLocal.get().nextInt(bound);
    }

    @Override
    public long nextLong() {
        return chunkRandomThreadLocal.get().nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return chunkRandomThreadLocal.get().nextBoolean();
    }

    @Override
    public float nextFloat() {
        return chunkRandomThreadLocal.get().nextFloat();
    }

    @Override
    public double nextDouble() {
        return chunkRandomThreadLocal.get().nextDouble();
    }

    @Override
    public synchronized double nextGaussian() {
        return chunkRandomThreadLocal.get().nextGaussian();
    }

    @Override
    public IntStream ints(long streamSize) {
        return chunkRandomThreadLocal.get().ints(streamSize);
    }

    @Override
    public IntStream ints() {
        return chunkRandomThreadLocal.get().ints();
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return chunkRandomThreadLocal.get().ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return chunkRandomThreadLocal.get().ints(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long streamSize) {
        return chunkRandomThreadLocal.get().longs(streamSize);
    }

    @Override
    public LongStream longs() {
        return chunkRandomThreadLocal.get().longs();
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return chunkRandomThreadLocal.get().longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return chunkRandomThreadLocal.get().longs(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return chunkRandomThreadLocal.get().doubles(streamSize);
    }

    @Override
    public DoubleStream doubles() {
        return chunkRandomThreadLocal.get().doubles();
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return chunkRandomThreadLocal.get().doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return chunkRandomThreadLocal.get().doubles(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public void consumeCount(int count) {
        chunkRandomThreadLocal.get().consumeCount(count);
    }

    @Override
    public int hashCode() {
        return chunkRandomThreadLocal.get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return chunkRandomThreadLocal.get().equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return chunkRandomThreadLocal.get().toString();
    }
}
