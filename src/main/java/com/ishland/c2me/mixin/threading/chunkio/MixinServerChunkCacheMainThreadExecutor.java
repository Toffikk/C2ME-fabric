package com.ishland.c2me.mixin.threading.chunkio;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkCache.MainThreadExecutor.class)
public abstract class MixinServerChunkCacheMainThreadExecutor extends BlockableEventLoop<Runnable> {

    protected MixinServerChunkCacheMainThreadExecutor(String name) {
        super(name);
    }

    @Inject(method = "runTask", at = @At("RETURN"))
    private void onPostRunTask(CallbackInfoReturnable<Boolean> cir) {
        super.pollTask();
    }

}
