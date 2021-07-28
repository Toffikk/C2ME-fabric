package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.BasaltDeltasSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.FrozenOceanSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GiantTreeTaigaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GravellyMountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.MountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherForestSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NopeSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ShatteredSavanaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SoulSandValleySurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SwampSurfaceBuilder;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public abstract class MixinSurfaceBuilder {

    @Shadow
    private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F register(String id, F surfaceBuilder) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Dynamic
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;register(Ljava/lang/String;Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;)Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;"))
    private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F redirectRegister(String id, F surfaceBuilder) {
        if (surfaceBuilder instanceof BadlandsSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new BadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC), SurfaceBuilderBaseConfiguration.CODEC));
        } else if (surfaceBuilder instanceof BasaltDeltasSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new BasaltDeltasSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC), SurfaceBuilderBaseConfiguration.CODEC));
        } else if (surfaceBuilder instanceof FrozenOceanSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new FrozenOceanSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC), SurfaceBuilderBaseConfiguration.CODEC));
        } else if (surfaceBuilder instanceof NetherForestSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new NetherForestSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC), SurfaceBuilderBaseConfiguration.CODEC));
        } else if (surfaceBuilder instanceof NetherSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new NetherSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC), SurfaceBuilderBaseConfiguration.CODEC));
        }
        if (!(surfaceBuilder instanceof DefaultSurfaceBuilder ||
                surfaceBuilder instanceof GiantTreeTaigaSurfaceBuilder ||
                surfaceBuilder instanceof GravellyMountainSurfaceBuilder ||
                surfaceBuilder instanceof MountainSurfaceBuilder ||
                surfaceBuilder instanceof NopeSurfaceBuilder ||
                surfaceBuilder instanceof ShatteredSavanaSurfaceBuilder ||
                surfaceBuilder instanceof SoulSandValleySurfaceBuilder ||
                surfaceBuilder instanceof SwampSurfaceBuilder)) {
            //noinspection RedundantStringFormatCall
            System.err.println(String.format("Warning: Unknown surface builder: %s. It may cause issues when using this with C2ME threaded worldgen.", surfaceBuilder.getClass().getName()));
        }
        return register(id, surfaceBuilder);
    }

}
