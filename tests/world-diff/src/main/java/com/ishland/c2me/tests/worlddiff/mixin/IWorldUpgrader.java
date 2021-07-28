package com.ishland.c2me.tests.worlddiff.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

@Mixin(WorldUpgrader.class)
public interface IWorldUpgrader {

    @Invoker
    List<ChunkPos> invokeGetAllChunkPos(ResourceKey<Level> world);

}
