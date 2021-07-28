package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;

@Mixin(StructureStart.class)
public class MixinStructureStart<C extends FeatureConfiguration> {

    @Mutable
    @Shadow
    @Final
    protected WorldgenRandom random;

    private final AtomicInteger referencesAtomic = new AtomicInteger();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(StructureFeature<C> feature, ChunkPos pos, int references, long seed, CallbackInfo ci) {
        this.random = new ThreadLocalChunkRandom(seed,
                chunkRandom -> chunkRandom.setLargeFeatureSeed(seed, pos.x, pos.z) // TODO [VanillaCopy]
        );
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StructureStart;references:I", opcode = Opcodes.GETFIELD))
    private int redirectGetReferences(StructureStart<?> structureStart) {
        return referencesAtomic.get();
    }

    /**
     * @author ishland
     * @reason atomic operation
     */
    @Overwrite
    public void incrementReferences() {
        this.referencesAtomic.incrementAndGet();
    }

}
