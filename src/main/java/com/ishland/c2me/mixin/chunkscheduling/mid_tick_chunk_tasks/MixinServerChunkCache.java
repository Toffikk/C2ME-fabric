package com.ishland.c2me.mixin.chunkscheduling.mid_tick_chunk_tasks;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ishland.c2me.common.chunkscheduling.ServerMidTickTask;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerChunkCache.class)
public class MixinServerChunkCache {

    @Shadow @Final private ServerLevel world;

    @Dynamic
    @Inject(method = "lambda$tickChunks$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V"))
    private void onPostTickChunk(CallbackInfo ci) { // TODO synthetic method - in tickChunks()
        ((ServerMidTickTask) this.world.getServer()).executeTasksMidTick();
    }

}
