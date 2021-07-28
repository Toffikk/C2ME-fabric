package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureFeature.class)
public class MixinStructureFeature {

    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBiomeAccess()Lnet/minecraft/world/biome/source/BiomeAccess;"), require = 0)
    private BiomeManager redirectBiomeAccess(LevelReader worldView) {
        if (worldView instanceof ServerLevel serverWorld) {
            return new BiomeManager(serverWorld.getChunkSource().getGenerator().getBiomeSource(), serverWorld.getSeed(), serverWorld.dimensionType().getBiomeZoomer());
        }
        return worldView.getBiomeManager();
    }

}
