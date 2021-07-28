package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues.math;

import com.mojang.math.Transformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Transformation.class)
public abstract class MixinTransformation {

    @Shadow protected abstract void init();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.init(); // run init early
    }

}
