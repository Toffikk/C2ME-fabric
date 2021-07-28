package com.ishland.c2me.mixin.threading.chunkio;

import com.ishland.c2me.common.threading.chunkio.AsyncSerializationManager;
import com.ishland.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;initForPalette(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/ChunkSection;)V"))
    private static void onPoiStorageInitForPalette(PoiManager pointOfInterestStorage, ChunkPos chunkPos, LevelChunkSection chunkSection) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> pointOfInterestStorage.checkConsistencyWithBlocks(chunkPos, chunkSection));
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getBlockEntityPositions()Ljava/util/Set;"))
    private static Set<BlockPos> onChunkGetBlockEntityPositions(ChunkAccess chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.blockEntities.keySet() : chunk.getBlockEntitiesPos();
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPackedBlockEntityNbt(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/nbt/NbtCompound;"))
    private static CompoundTag onChunkGetPackedBlockEntityNbt(ChunkAccess chunk, BlockPos pos) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        if (scope == null) return chunk.getBlockEntityNbtForSaving(pos);
        final BlockEntity blockEntity = scope.blockEntities.get(pos);
        if (blockEntity == null) return null;
        final CompoundTag compoundTag = new CompoundTag();
        if (chunk instanceof LevelChunk) compoundTag.putBoolean("keepPacked", false);
        blockEntity.save(compoundTag);
        return compoundTag;
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getBlockTickScheduler()Lnet/minecraft/world/TickScheduler;"))
    private static TickList<Block> onChunkGetBlockTickScheduler(ChunkAccess chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.blockTickScheduler : chunk.getBlockTicks();
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getFluidTickScheduler()Lnet/minecraft/world/TickScheduler;"))
    private static TickList<Fluid> onChunkGetFluidTickScheduler(ChunkAccess chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.fluidTickScheduler : chunk.getLiquidTicks();
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;toNbt(Lnet/minecraft/util/math/ChunkPos;)Lnet/minecraft/nbt/NbtList;"))
    private static ListTag onServerTickSchedulerToNbt(@SuppressWarnings("rawtypes") ServerTickList serverTickScheduler, ChunkPos chunkPos) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunkPos);
        return scope != null ? CompletableFuture.supplyAsync(() -> serverTickScheduler.save(chunkPos), serverTickScheduler.level.chunkSource.mainThreadProcessor).join() : serverTickScheduler.save(chunkPos);
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;get(Lnet/minecraft/world/LightType;)Lnet/minecraft/world/chunk/light/ChunkLightingView;"))
    private static LayerLightEventListener onLightingProviderGet(LevelLightEngine lightingProvider, LightLayer lightType) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(null);
        return scope != null ? scope.lighting.get(lightType) : lightingProvider.getLayerListener(lightType);
    }

}
