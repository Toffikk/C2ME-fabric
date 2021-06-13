package org.yatopiamc.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

@Mixin(ChunkMap.class)
public abstract class MixinThreadedChunkAnvilStorage {

    @Shadow protected abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> loadChunk(ChunkPos pos);

    @Shadow @Final private ChunkGenerator chunkGenerator;

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;loadChunk(Lnet/minecraft/util/math/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> redirectLoadChunk(ChunkMap threadedAnvilChunkStorage, ChunkPos pos) {
        return this.loadChunk(pos).thenApplyAsync(either -> {
            if (chunkGenerator.getBiomeSource() instanceof IVanillaLayeredBiomeSource source) {
                either.left().ifPresent(chunk -> {
                    final BiomeArray biomeArray = source.preloadBiomes(chunk, pos, chunk.getBiomeArray());
                    if (chunk instanceof ProtoChunk protoChunk) protoChunk.setBiomes(biomeArray);
                });
            }
            return either;
        }, BiomeCache.EXECUTOR);
    }

}
