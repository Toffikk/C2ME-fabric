package com.ishland.c2me.mixin.util.math;

import com.ishland.c2me.common.util.IBiomeArray;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkBiomeContainer.class)
public class MixinChunkBiomeContainer implements IBiomeArray {


    @Shadow @Final private int quartMinY; // bottomY

    @Shadow @Final private int quartHeight; // (height - 1)

    @Override
    public boolean isWithinRange(int y) {
        return y >= quartMinY && y <= quartHeight + quartMinY;
    }
}
