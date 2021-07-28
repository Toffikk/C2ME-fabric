package com.ishland.c2me.mixin.chunkscheduling.mid_tick_chunk_tasks;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.ishland.c2me.common.chunkscheduling.ServerMidTickTask;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerMidTickTask {

    @Shadow public abstract Iterable<ServerLevel> getWorlds();

    @Shadow @Final private Thread serverThread;
    private static final long minMidTickTaskInterval = 25_000L; // 25us
    private final AtomicLong lastRun = new AtomicLong(System.nanoTime());

    public void executeTasksMidTick() {
        if (this.serverThread != Thread.currentThread()) return;
        if (System.nanoTime() - lastRun.get() < minMidTickTaskInterval) return;
        for (ServerLevel world : this.getWorlds()) {
            world.chunkSource.mainThreadProcessor.pollTask();
        }
        lastRun.set(System.nanoTime());
    }

}
