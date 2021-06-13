package org.yatopiamc.c2me.mixin.chunkscheduling.mid_tick_chunk_tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.chunkscheduling.ServerMidTickTask;

@Mixin(ServerTickList.class)
public class MixinServerTickScheduler {

    @Shadow @Final public ServerLevel world;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onPostActionTick(CallbackInfo ci) {
        ((ServerMidTickTask) this.world.getServer()).executeTasksMidTick();
    }

}
