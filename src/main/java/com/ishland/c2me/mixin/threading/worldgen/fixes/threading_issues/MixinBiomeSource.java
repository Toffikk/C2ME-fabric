package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

@Mixin(BiomeSource.class)
public abstract class MixinBiomeSource {

    @Mutable
    @Shadow @Final protected Map<StructureFeature<?>, Boolean> structureFeatures;

    @Shadow public abstract Set<BlockState> getTopMaterials();

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.structureFeatures = Collections.synchronizedMap(structureFeatures);
        this.getTopMaterials(); // init early
    }

}
