package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BiomeSource.class)
public class MixinBiomeSource {

    @Redirect(method = {"getBiomesInArea", "locateBiome(IIIIILjava/util/function/Predicate;Ljava/util/Random;Z)Lnet/minecraft/util/math/BlockPos;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeSource;getBiomeForNoiseGen(III)Lnet/minecraft/world/biome/Biome;"))
    private Biome redirectGetBiomeForNoiseGen(BiomeSource biomeSource, int biomeX, int biomeY, int biomeZ) {
        if (biomeSource instanceof IGlobalBiomeCache globalBiomeCache) {
            return globalBiomeCache.getBiomeForNoiseGenFast(biomeX, biomeY, biomeZ);
        }
        return biomeSource.getNoiseBiome(biomeX, biomeY, biomeZ);
    }

}
