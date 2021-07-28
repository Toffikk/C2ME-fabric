package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;

public interface IGlobalBiomeCache {

    ChunkBiomeContainer preloadBiomes(LevelHeightAccessor view, ChunkPos pos, ChunkBiomeContainer def);

    Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ);
}
