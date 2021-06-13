package org.yatopiamc.c2me.mixin.optimization.worldgen.threadlocal_block_interpolator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DepthBasedReplacingBaseStoneSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;

@Mixin(DepthBasedReplacingBaseStoneSource.class)
public class MixinDeepslateInterpolator {

    private ThreadLocal<WorldgenRandom> chunkRandomThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, BlockState defaultBlock, BlockState deepslateState, NoiseGeneratorSettings chunkGeneratorSettings, CallbackInfo ci) {
        chunkRandomThreadLocal = ThreadLocal.withInitial(() -> new WorldgenRandom(seed));
    }

    @Redirect(method = "sample", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/DeepslateBlockSource;random:Lnet/minecraft/world/gen/ChunkRandom;"))
    private WorldgenRandom redirectRandomUsage(DepthBasedReplacingBaseStoneSource source) {
        return chunkRandomThreadLocal.get();
    }

}
