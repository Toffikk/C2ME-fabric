package com.ishland.c2me.common.optimization.worldgen.threadsafe_weightedlist;

import net.minecraft.world.entity.ai.behavior.ShufflingList;

public interface IShufflingList<U> {

    public ShufflingList<U> shuffleVanilla();

}
