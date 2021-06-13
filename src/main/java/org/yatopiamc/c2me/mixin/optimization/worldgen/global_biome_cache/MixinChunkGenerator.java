package org.yatopiamc.c2me.mixin.optimization.worldgen.global_biome_cache;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Shadow @Final protected BiomeSource biomeSource;

    @Inject(method = "populateBiomes", at = @At("HEAD"), cancellable = true)
    private void onPopulateBiomes(Registry<Biome> biomeRegistry, ChunkAccess chunk, CallbackInfo ci) {
        if (biomeSource instanceof IVanillaLayeredBiomeSource biomeSource1) {
            ((ProtoChunk) chunk).setBiomes(biomeSource1.preloadBiomes(chunk, chunk.getPos(), chunk.getBiomeArray()));
            ci.cancel();
        }
    }

}
