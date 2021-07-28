package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalNoiseInterpolator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseInterpolator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.OreVeinNoiseSource.class)
public class MixinNoiseBasedChunkGeneratorOreVeinNoiseSource {

    @Mutable
    @Shadow @Final private WorldgenRandom random;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.random = new ThreadLocalChunkRandom(System.nanoTime());
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/NoiseInterpolator"))
    private NoiseInterpolator redirectNoiseInterpolator(int sizeX, int sizeY, int sizeZ, ChunkPos pos, int minY, NoiseInterpolator.NoiseColumnFiller columnSampler) {
        return new ThreadLocalNoiseInterpolator(sizeX, sizeY, sizeZ, pos, minY, columnSampler);
    }

}
