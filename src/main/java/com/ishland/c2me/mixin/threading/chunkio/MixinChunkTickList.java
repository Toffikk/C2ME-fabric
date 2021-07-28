package com.ishland.c2me.mixin.threading.chunkio;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.ishland.c2me.common.util.DeepCloneable;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.TickList;

@Mixin(ChunkTickList.class)
public abstract class MixinChunkTickList<T> implements DeepCloneable {

    @Shadow @Final private Function<T, ResourceLocation> identifierProvider;

    @Shadow public abstract void scheduleTo(TickList<T> scheduler);

    @Override
    public Object deepClone() {
        final ChunkTickList<T> scheduler = new ChunkTickList<>(identifierProvider, new ObjectArrayList<>());
        scheduleTo(scheduler);
        return scheduler;
    }
}
