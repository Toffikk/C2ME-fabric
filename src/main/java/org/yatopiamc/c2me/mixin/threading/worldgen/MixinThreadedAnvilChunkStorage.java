package org.yatopiamc.c2me.mixin.threading.worldgen;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

@Mixin(ChunkMap.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ServerLevel world;
    @Shadow @Final private BlockableEventLoop<Runnable> mainThreadExecutor;

    private final Executor mainInvokingExecutor = runnable -> {
        if (this.world.getServer().isSameThread()) {
            runnable.run();
        } else {
            this.mainThreadExecutor.execute(runnable);
        }
    };

    private final ThreadLocal<ChunkStatus> capturedRequiredStatus = new ThreadLocal<>();

    @Inject(method = "upgradeChunk", at = @At("HEAD"))
    private void onUpgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        capturedRequiredStatus.set(requiredStatus);
    }

    @Redirect(method = "makeChunkEntitiesTickable", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <U, T> CompletableFuture<U> redirectMainThreadExecutor1(CompletableFuture<T> completableFuture, Function<? super T, ? extends U> fn, Executor executor) {
        return completableFuture.thenApplyAsync(fn, this.mainInvokingExecutor);
    }

    /**
     * @author ishland
     * @reason move to scheduler & improve chunk status transition speed
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_17259(ChunkHolder chunkHolder, Runnable runnable) { // synthetic method for worldGenExecutor scheduling in upgradeChunk
        final ChunkStatus capturedStatus = capturedRequiredStatus.get();
        capturedRequiredStatus.remove();
        if (capturedStatus != null) {
            final ChunkAccess currentChunk = chunkHolder.getLastAvailable();
            if (currentChunk != null && currentChunk.getStatus().isOrAfter(capturedStatus)) {
                this.mainInvokingExecutor.execute(runnable);
                return;
            }
        }
        GlobalExecutors.scheduler.execute(runnable);
    }

}
