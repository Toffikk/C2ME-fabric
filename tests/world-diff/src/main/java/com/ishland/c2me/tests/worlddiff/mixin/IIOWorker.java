package com.ishland.c2me.tests.worlddiff.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;

@Mixin(IOWorker.class)
public interface IIOWorker {

    @Invoker
    CompletableFuture<CompoundTag> invokeLoadAsync(ChunkPos pos);

}
