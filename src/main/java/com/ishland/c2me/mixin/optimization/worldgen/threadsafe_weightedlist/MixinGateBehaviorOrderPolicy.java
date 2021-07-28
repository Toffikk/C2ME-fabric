package com.ishland.c2me.mixin.optimization.worldgen.threadsafe_weightedlist;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ishland.c2me.common.optimization.worldgen.threadsafe_weightedlist.IShufflingList;

import java.util.function.Consumer;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

@Mixin(GateBehavior.OrderPolicy.class)
public class MixinGateBehaviorOrderPolicy {

    @Mutable
    @Shadow @Final private Consumer<ShufflingList<?>> listModifier;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(String enumName, int ordinal, Consumer<ShufflingList<?>> listModifier, CallbackInfo ci) {
        if (enumName.equals("field_18349") || enumName.equals("SHUFFLED"))
            this.listModifier = obj -> ((IShufflingList<?>) obj).shuffleVanilla();
    }

}
