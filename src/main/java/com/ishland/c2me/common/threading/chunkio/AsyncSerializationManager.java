package com.ishland.c2me.common.threading.chunkio;

import com.ishland.c2me.common.util.DeepCloneable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;

public class AsyncSerializationManager {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Async Serialization Manager");

    private static final ThreadLocal<ArrayDeque<Scope>> scopeHolder = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(Scope scope) {
        scopeHolder.get().push(scope);
    }

    public static Scope getScope(ChunkPos pos) {
        final Scope scope = scopeHolder.get().peek();
        if (pos == null) return scope;
        if (scope != null) {
            if (scope.pos.equals(pos))
                return scope;
            LOGGER.error("Scope position mismatch! Expected: {} but got {}. This will impact stability. Incompatible mods?", scope.pos, pos, new Throwable());
        }
        return null;
    }

    public static void pop(Scope scope) {
        if (scope != scopeHolder.get().peek()) throw new IllegalArgumentException("Scope mismatch");
        scopeHolder.get().pop();
    }

    public static class Scope {
        public final ChunkPos pos;
        public final Map<LightLayer, LayerLightEventListener> lighting;
        public final TickList<Block> blockTickScheduler;
        public final TickList<Fluid> fluidTickScheduler;
        public final Map<BlockPos, BlockEntity> blockEntities;
        private final AtomicBoolean isOpen = new AtomicBoolean(false);

        @SuppressWarnings("unchecked")
        public Scope(ChunkAccess chunk, ServerLevel world) {
            this.pos = chunk.getPos();
            this.lighting = Arrays.stream(LightLayer.values()).map(type -> new CachedLightingView(world.getLightEngine(), chunk.getPos(), type)).collect(Collectors.toMap(CachedLightingView::getLightType, Function.identity()));
            final TickList<Block> blockTickScheduler = chunk.getBlockTicks();
            if (blockTickScheduler instanceof DeepCloneable cloneable) {
                this.blockTickScheduler = (TickList<Block>) cloneable.deepClone();
            } else {
                final ServerTickList<Block> worldBlockTickScheduler = world.getBlockTicks();
                this.blockTickScheduler = new ChunkTickList<>(Registry.BLOCK::getKey, worldBlockTickScheduler.fetchTicksInChunk(chunk.getPos(), false, true), world.getGameTime());
            }
            final TickList<Fluid> fluidTickScheduler = chunk.getLiquidTicks();
            if (fluidTickScheduler instanceof DeepCloneable cloneable) {
                this.fluidTickScheduler = (TickList<Fluid>) cloneable.deepClone();
            } else {
                final ServerTickList<Fluid> worldFluidTickScheduler = world.getLiquidTicks();
                this.fluidTickScheduler = new ChunkTickList<>(Registry.FLUID::getKey, worldFluidTickScheduler.fetchTicksInChunk(chunk.getPos(), false, true), world.getGameTime());
            }
            this.blockEntities = chunk.getBlockEntitiesPos().stream().map(chunk::getBlockEntity).filter(Objects::nonNull).filter(blockEntity -> !blockEntity.isRemoved()).collect(Collectors.toMap(BlockEntity::getBlockPos, Function.identity()));
        }

        public void open() {
            if (!isOpen.compareAndSet(false, true)) throw new IllegalStateException("Cannot use scope twice");
        }

        private static final class CachedLightingView implements LayerLightEventListener {

            private static final DataLayer EMPTY = new DataLayer();

            private final LightLayer lightType;
            private final Map<SectionPos, DataLayer> cachedData = new Object2ObjectOpenHashMap<>();

            CachedLightingView(LevelLightEngine provider, ChunkPos pos, LightLayer type) {
                this.lightType = type;
                for (int i = provider.getMinLightSection(); i < provider.getMaxLightSection(); i++) {
                    final SectionPos sectionPos = SectionPos.of(pos, i);
                    DataLayer lighting = provider.getLayerListener(type).getDataLayerData(sectionPos);
                    cachedData.put(sectionPos, lighting != null ? lighting.copy() : null);
                }
            }

            public LightLayer getLightType() {
                return this.lightType;
            }

            @Override
            public void checkBlock(BlockPos blockPos) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasLightWork() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int runUpdates(int i, boolean bl, boolean bl2) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void updateSectionStatus(SectionPos pos, boolean notReady) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void enableLightSources(ChunkPos chunkPos, boolean bl) {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public DataLayer getDataLayerData(SectionPos pos) {
                return cachedData.getOrDefault(pos, EMPTY);
            }

            @Override
            public int getLightValue(BlockPos pos) {
                throw new UnsupportedOperationException();
            }
        }
    }

}
