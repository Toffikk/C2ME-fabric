package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.ibm.asyncutil.locks.AsyncLock;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;

import java.io.File;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
public abstract class MixinVersionedChunkStorage {

    @Shadow @Final protected DataFixer dataFixer;

    @Shadow @Nullable private LegacyStructureDataHandler featureUpdater;

    private AsyncLock featureUpdaterLock = AsyncLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.featureUpdaterLock = AsyncLock.createFair();
    }

    /**
     * @author ishland
     * @reason async loading
     */
    @Overwrite
    public CompoundTag updateChunkNbt(ResourceKey<Level> registryKey, Supplier<DimensionDataStorage> persistentStateManagerFactory, CompoundTag tag) {
        // TODO [VanillaCopy] - check when updating minecraft version
        int i = ChunkStorage.getVersion(tag);
        if (i < 1493) {
            try (final AsyncLock.LockToken ignored = featureUpdaterLock.acquireLock().toCompletableFuture().join()) { // C2ME - async chunk loading
                tag = NbtUtils.update(this.dataFixer, DataFixTypes.CHUNK, tag, i, 1493);
                if (tag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                    if (this.featureUpdater == null) {
                        this.featureUpdater = LegacyStructureDataHandler.getLegacyStructureHandler(registryKey, (DimensionDataStorage)persistentStateManagerFactory.get());
                    }

                    tag = this.featureUpdater.updateFromLegacy(tag);
                }
            } // C2ME - async chunk loading
        }

        tag = NbtUtils.update(this.dataFixer, DataFixTypes.CHUNK, tag, Math.max(1493, i));
        if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
            tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        }

        return tag;
    }

    @Redirect(method = "setNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/FeatureUpdater;markResolved(J)V"))
    private void onSetTagAtFeatureUpdaterMarkResolved(LegacyStructureDataHandler featureUpdater, long l) {
        try (final AsyncLock.LockToken ignored = featureUpdaterLock.acquireLock().toCompletableFuture().join()) {
            featureUpdater.removeIndex(l);
        }
    }

}
