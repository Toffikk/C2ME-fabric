package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
public abstract class MixinOverworldBiomeSource extends BiomeSource implements IGlobalBiomeCache {

    protected MixinOverworldBiomeSource(List<Biome> biomes) {
        super(biomes);
    }

    private BiomeCache cacheImpl = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo info) {
        final ThreadLocal<Layer> samplerThreadLocal = ThreadLocal.withInitial(() -> Layers.getDefaultLayer(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4));
        this.cacheImpl = new BiomeCache((biomeRegistry1, x, y, z) -> samplerThreadLocal.get().get(biomeRegistry1, x, z), biomeRegistry, possibleBiomes);
    }

    /**
     * @author ishland
     * @reason re-implement caching
     */
    @Overwrite
    public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, false);
    }

    @Override
    public Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, true);
    }

    @Override
    public ChunkBiomeContainer preloadBiomes(LevelHeightAccessor view, ChunkPos pos, ChunkBiomeContainer def) {
        return cacheImpl.preloadBiomes(view, pos, def);
    }

}
