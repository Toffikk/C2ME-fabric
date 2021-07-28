package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RandomizedIntStateProvider.class)
public class MixinRandomizedIntStateProvider {

    @Shadow @Nullable private IntegerProperty property;

    @Redirect(method = "getBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/stateprovider/RandomizedIntBlockStateProvider;property:Lnet/minecraft/state/property/IntProperty;", opcode = Opcodes.PUTFIELD))
    private void redirectGetProperty(RandomizedIntStateProvider randomizedIntBlockStateProvider, IntegerProperty value) {
        System.err.println("Detected different property settings in RandomizedIntBlockStateProvider! Expected " + this.property + " but got " + value);
        this.property = value;
    }

}
