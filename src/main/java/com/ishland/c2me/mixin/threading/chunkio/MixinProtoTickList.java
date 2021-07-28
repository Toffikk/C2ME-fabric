package com.ishland.c2me.mixin.threading.chunkio;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.ishland.c2me.common.util.DeepCloneable;

import java.util.function.Predicate;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoTickList;

@Mixin(ProtoTickList.class)
public abstract class MixinProtoTickList<T> implements DeepCloneable {

    @Shadow
    public abstract ListTag toNbt();

    @Shadow
    @Final
    private ChunkPos pos;
    @Shadow @Final protected Predicate<T> shouldExclude;
    @Shadow private LevelHeightAccessor world;

    public ProtoTickList<T> deepClone() {
        return new ProtoTickList<>(shouldExclude, pos, toNbt(), world);
    }
}
