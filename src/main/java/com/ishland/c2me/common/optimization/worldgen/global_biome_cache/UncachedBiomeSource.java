package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;

public class UncachedBiomeSource extends BiomeSource {
    private final BiomeCache.BiomeProvider sampler;
    private final Registry<Biome> registry;

    public UncachedBiomeSource(List<Biome> biomes, BiomeCache.BiomeProvider sampler, Registry<Biome> registry) {
        super(biomes);
        this.sampler = sampler;
        this.registry = registry;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return OverworldBiomeSource.CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return sampler.sample(this.registry, biomeX, biomeY, biomeZ);
    }
}
