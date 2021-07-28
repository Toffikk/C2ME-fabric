package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Shadow @Final protected BiomeSource biomeSource;

    @Inject(method = "populateBiomes", at = @At("HEAD"), cancellable = true)
    private void onPopulateBiomes(Registry<Biome> biomeRegistry, ChunkAccess chunk, CallbackInfo ci) {
        if (biomeSource instanceof IGlobalBiomeCache biomeSource1) {
            ((ProtoChunk) chunk).setBiomes(biomeSource1.preloadBiomes(chunk, chunk.getPos(), chunk.getBiomes()));
            ci.cancel();
        }
    }

}
