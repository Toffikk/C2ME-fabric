package org.yatopiamc.c2me.mixin.chunkscheduling.fix_unload;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.structs.LongHashSet;
import org.yatopiamc.c2me.common.util.ShouldKeepTickingUtils;

import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

@Mixin(ChunkMap.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private BlockableEventLoop<Runnable> mainThreadExecutor;

    @Shadow protected abstract void unloadChunks(BooleanSupplier shouldKeepTicking);

    @Mutable
    @Shadow @Final private LongSet unloadedChunks;

    /**
     * @author ishland
     * @reason Queue unload immediately
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_20579(ChunkHolder holder, Runnable runnable) { // TODO synthetic method in thenApplyAsync call of makeChunkAccessible
        this.mainThreadExecutor.execute(runnable);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void redirectTickPointOfInterestStorageTick(PoiManager pointOfInterestStorage, BooleanSupplier shouldKeepTicking) {
        pointOfInterestStorage.tick(ShouldKeepTickingUtils.minimumTicks(shouldKeepTicking, 32));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;unloadChunks(Ljava/util/function/BooleanSupplier;)V"))
    private void redirectTickUnloadChunks(ChunkMap threadedAnvilChunkStorage, BooleanSupplier shouldKeepTicking) {
        this.unloadChunks(ShouldKeepTickingUtils.minimumTicks(shouldKeepTicking, 32));
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.unloadedChunks = new LongHashSet();
    }

}
