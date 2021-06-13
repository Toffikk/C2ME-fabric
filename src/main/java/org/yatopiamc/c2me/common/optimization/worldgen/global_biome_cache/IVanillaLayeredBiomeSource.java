package org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;

public interface IVanillaLayeredBiomeSource {

    ChunkBiomeContainer preloadBiomes(LevelHeightAccessor view, ChunkPos pos, ChunkBiomeContainer def);
}
