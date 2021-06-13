package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkCache.MainThreadExecutor.class)
public abstract class MixinServerChunkManagerMainThreadExecutor extends BlockableEventLoop<Runnable> {

    protected MixinServerChunkManagerMainThreadExecutor(String name) {
        super(name);
    }

    @Inject(method = "pollTask", at = @At("RETURN"))
    private void onPostPollTask(CallbackInfoReturnable<Boolean> cir) {
        super.pollTask();
    }

}
