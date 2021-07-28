package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import net.minecraft.world.level.levelgen.DepthBasedReplacingBaseStoneSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DepthBasedReplacingBaseStoneSource.class)
public class MixinDepthBasedReplacingBaseStoneSource {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/ChunkRandom"))
    private WorldgenRandom redirectNewChunkRandom(long seed) {
        return new ThreadLocalChunkRandom(seed);
    }

}
