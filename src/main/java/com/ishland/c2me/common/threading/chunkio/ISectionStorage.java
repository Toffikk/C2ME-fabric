package com.ishland.c2me.common.threading.chunkio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface ISectionStorage {

    void update(ChunkPos pos, CompoundTag tag);

}
