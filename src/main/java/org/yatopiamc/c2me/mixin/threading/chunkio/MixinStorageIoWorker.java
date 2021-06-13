package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.mojang.datafixers.util.Either;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import org.yatopiamc.c2me.common.threading.chunkio.IAsyncChunkStorage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

@Mixin(IOWorker.class)
public abstract class MixinStorageIoWorker implements IAsyncChunkStorage {

    @Mutable
    @Shadow @Final private RegionFileStorage storage;

    @Mutable
    @Shadow @Final private Map<ChunkPos, IOWorker.PendingStore> results;

    @Shadow @Final private AtomicBoolean closed;

    @Mutable
    @Shadow @Final private ProcessorMailbox<StrictQueue.IntRunnable> executor;

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> supplier);

    @Shadow protected abstract CompletableFuture<CompoundTag> readChunkData(ChunkPos pos);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onPostInit(CallbackInfo info) {
        //noinspection ConstantConditions
        if (((Object) this) instanceof C2MECachedRegionStorage) {
            this.storage = null;
            this.results = null;
            this.executor = null;
            this.closed.set(true);
        }
    }

    private final AtomicReference<ExecutorService> executorService = new AtomicReference<>();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getIoWorkerExecutor()Ljava/util/concurrent/Executor;"))
    private Executor onGetStorageIoWorker() {
        executorService.set(Executors.newSingleThreadExecutor(ChunkIoThreadingExecutorUtils.ioWorkerFactory));
        return executorService.get();
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/TaskExecutor;close()V", shift = At.Shift.AFTER))
    private void onClose(CallbackInfo ci) {
        final ExecutorService executorService = this.executorService.get();
        if (executorService != null) executorService.shutdown();
    }

    @Override
    public CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos) {
        return readChunkData(pos);
    }
}
