package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.util.worldupdate.WorldUpgrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldUpgrader.class)
public class MixinWorldUpgrader {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private void redirectThreadStart(Thread thread) {
    }

}
