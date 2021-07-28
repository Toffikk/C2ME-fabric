package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalBeardifier;
import net.minecraft.world.level.levelgen.Beardifier;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Beardifier.class)
public class MixinBeardifier {

    @Mutable
    @Shadow @Final public static Beardifier INSTANCE;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        INSTANCE = new ThreadLocalBeardifier();
    }

}
