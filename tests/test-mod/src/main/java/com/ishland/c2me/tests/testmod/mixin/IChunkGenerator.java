package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGenerator.class)
public interface IChunkGenerator {

    @Accessor
    BiomeSource getBiomeSource();

}
