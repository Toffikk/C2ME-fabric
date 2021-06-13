package org.yatopiamc.c2me.mixin.threading.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

@Mixin(NoiseBasedChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @Redirect(method = "populateNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/Executor;"))
    private Executor onPopulateNoiseGetExecutor() {
        return Runnable::run;
    }

}
