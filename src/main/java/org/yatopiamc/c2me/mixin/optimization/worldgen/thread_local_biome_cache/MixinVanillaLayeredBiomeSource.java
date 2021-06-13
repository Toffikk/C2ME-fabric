package org.yatopiamc.c2me.mixin.optimization.worldgen.thread_local_biome_cache;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OverworldBiomeSource.class, priority = 1050)
public class MixinVanillaLayeredBiomeSource {

    private ThreadLocal<Layer> biomeLayerSamplerThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo ci) {
        biomeLayerSamplerThreadLocal = ThreadLocal.withInitial(() -> Layers.getDefaultLayer(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4));
    }

    @Redirect(method = "getBiomeForNoiseGen", at = @At(value = "FIELD", target = "Lnet/minecraft/world/biome/source/VanillaLayeredBiomeSource;biomeSampler:Lnet/minecraft/world/biome/source/BiomeLayerSampler;"))
    private Layer redirectSamplerUsage(OverworldBiomeSource unused) {
        return biomeLayerSamplerThreadLocal.get();
    }

}
