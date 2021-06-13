package org.yatopiamc.c2me.common.threading.worldgen;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;

public enum ChunkStatusThreadingType {

    PARALLELIZED() {
        @Override
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture) {
            return CompletableFuture.supplyAsync(completableFuture, WorldGenThreadingExecutorUtils.mainExecutor).thenCompose(Function.identity());
        }
    },
    SINGLE_THREADED() {
        @Override
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture) {
            Preconditions.checkNotNull(lock);
            return lock.acquireLock().toCompletableFuture().thenComposeAsync(lockToken -> {
                try {
                    return completableFuture.get();
                } finally {
                    lockToken.releaseLock();
                }
            }, WorldGenThreadingExecutorUtils.mainExecutor);
        }
    },
    AS_IS() {
        @Override
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture) {
            return completableFuture.get();
        }
    };

    public abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture);

}
