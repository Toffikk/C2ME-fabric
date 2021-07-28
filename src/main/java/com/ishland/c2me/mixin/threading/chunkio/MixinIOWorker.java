package com.ishland.c2me.mixin.threading.chunkio;

import com.ishland.c2me.common.threading.chunkio.IAsyncChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;

@Mixin(IOWorker.class)
public abstract class MixinIOWorker implements IAsyncChunkStorage {

    @Shadow protected abstract CompletableFuture<CompoundTag> readChunkData(ChunkPos pos);

    @Override
    public CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos) {
        return readChunkData(pos);
    }
}
