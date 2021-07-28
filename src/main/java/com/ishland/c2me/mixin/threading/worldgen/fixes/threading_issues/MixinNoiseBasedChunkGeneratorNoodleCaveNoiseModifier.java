package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalNoiseInterpolator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseInterpolator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseBasedChunkGenerator.NoodleCaveNoiseModifier.class)
public class MixinNoiseBasedChunkGeneratorNoodleCaveNoiseModifier {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/NoiseInterpolator"))
    private NoiseInterpolator redirectNoiseInterpolator(int sizeX, int sizeY, int sizeZ, ChunkPos pos, int minY, NoiseInterpolator.NoiseColumnFiller columnSampler) {
        return new ThreadLocalNoiseInterpolator(sizeX, sizeY, sizeZ, pos, minY, columnSampler);
    }

}
