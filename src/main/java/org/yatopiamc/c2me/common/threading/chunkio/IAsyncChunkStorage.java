package org.yatopiamc.c2me.common.threading.chunkio;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface IAsyncChunkStorage {

    CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos);

}
