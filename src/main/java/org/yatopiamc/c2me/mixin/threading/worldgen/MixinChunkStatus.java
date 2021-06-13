package org.yatopiamc.c2me.mixin.threading.worldgen;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.common.threading.worldgen.ChunkStatusUtils;
import org.yatopiamc.c2me.common.threading.worldgen.IChunkStatus;
import org.yatopiamc.c2me.common.threading.worldgen.IWorldGenLockable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@Mixin(ChunkStatus.class)
public abstract class MixinChunkStatus implements IChunkStatus {

    @Shadow
    @Final
    private ChunkStatus.GenerationTask generationTask;

    @Shadow
    @Final
    private int taskMargin;

    private int reducedTaskRadius = -1;

    public void calculateReducedTaskRadius() {
        if (this.taskMargin == 0) {
            this.reducedTaskRadius = 0;
        } else {
            for (int i = 0; i <= this.taskMargin; i++) {
                final ChunkStatus status = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance((ChunkStatus) (Object) this) + i); // TODO [VanillaCopy] from TACS getRequiredStatusForGeneration
                if (status == ChunkStatus.STRUCTURE_STARTS) {
                    this.reducedTaskRadius = Math.min(this.taskMargin, i);
                    break;
                }
            }
        }
        //noinspection ConstantConditions
        if ((Object) this == ChunkStatus.LIGHT) {
            this.reducedTaskRadius = 1;
        }
        System.out.printf("%s task radius: %d -> %d%n", this, this.taskMargin, this.reducedTaskRadius);
    }

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        for (ChunkStatus chunkStatus : Registry.CHUNK_STATUS) {
            ((IChunkStatus) chunkStatus).calculateReducedTaskRadius();
        }
    }

    /**
     * @author ishland
     * @reason take over generation & improve chunk status transition speed
     */
    @Overwrite
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> runGenerationTask(Executor executor, ServerLevel serverWorld, ChunkGenerator chunkGenerator, StructureManager structureManager, ThreadedLevelLightEngine serverLightingProvider, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list) {
        final ChunkAccess targetChunk = list.get(list.size() / 2);
        final Supplier<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> generationTask = () ->
                this.generationTask.doWork((ChunkStatus) (Object) this, executor, serverWorld, chunkGenerator, structureManager, serverLightingProvider, function, list, targetChunk);
        if (targetChunk.getStatus().isOrAfter((ChunkStatus) (Object) this)) {
            return generationTask.get();
        } else {
            int lockRadius = C2MEConfig.threadedWorldGenConfig.reduceLockRadius && this.reducedTaskRadius != -1 ? this.reducedTaskRadius : this.taskMargin;
            //noinspection ConstantConditions
            return ChunkStatusUtils.runChunkGenWithLock(targetChunk.getPos(), lockRadius, ((IWorldGenLockable) serverWorld).getWorldGenChunkLock(), () ->
                    ChunkStatusUtils.getThreadingType((ChunkStatus) (Object) this).runTask(((IWorldGenLockable) serverWorld).getWorldGenSingleThreadedLock(), generationTask));
        }
    }

}
