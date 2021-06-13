package org.yatopiamc.c2me.mixin.optimization.worldgen.global_biome_cache;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;

@Mixin(OverworldBiomeSource.class)
public abstract class MixinVanillaLayeredBiomeSource extends BiomeSource implements IVanillaLayeredBiomeSource {

    @Shadow @Final private Layer biomeSampler;

    @Shadow @Final private Registry<Biome> biomeRegistry;

    protected MixinVanillaLayeredBiomeSource(List<Biome> biomes) {
        super(biomes);
    }

    private BiomeCache cacheImpl = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo info) {
        this.cacheImpl = new BiomeCache(ThreadLocal.withInitial(() -> Layers.getDefaultLayer(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4)), biomeRegistry, possibleBiomes);
    }

    /**
     * @author ishland
     * @reason re-implement caching
     */
    @Overwrite
    public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }

    @Override
    public ChunkBiomeContainer preloadBiomes(LevelHeightAccessor view, ChunkPos pos, ChunkBiomeContainer def) {
        return cacheImpl.preloadBiomes(view, pos, def);
    }

}
