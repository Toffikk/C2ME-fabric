package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface IChunkMap {

    @Accessor
    BlockableEventLoop<Runnable> getMainThreadExecutor();

    @Invoker
    ChunkHolder invokeGetVisibleChunkIfPresent(long pos);

}
