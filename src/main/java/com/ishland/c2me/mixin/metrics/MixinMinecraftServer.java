package com.ishland.c2me.mixin.metrics;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.metrics.Metrics;
import net.minecraft.DetectedVersion;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {


    @SuppressWarnings("ConstantConditions")
    @Inject(method = "runServer", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"))
    private void onInit(CallbackInfo info) {
        final Metrics metrics = new Metrics(10514, (MinecraftServer) (Object) this);
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.enabled)));
        metrics.addCustomChart(new Metrics.SimplePie("serverType", () -> {
            if ((Object) this instanceof DedicatedServer) return "Dedicated Server";
            if ((Object) this instanceof IntegratedServer) return "Integrated Server";
            if ((Object) this instanceof GameTestServer) return "Test Server";
            return "Unknown: " + this.getClass().getName();
        }));
        if (C2MEConfig.threadedWorldGenConfig.enabled) {
            metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldFeatureGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.allowThreadedFeatures)));
            metrics.addCustomChart(new Metrics.SimplePie("useReducedLockRadius", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.reduceLockRadius)));
            metrics.addCustomChart(new Metrics.SimplePie("useGlobalBiomeCache", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.useGlobalBiomeCache)));
        }
        metrics.addCustomChart(new Metrics.DrilldownPie("detailedMinecraftVersion", () -> ImmutableMap.of(DetectedVersion.BUILT_IN.getReleaseTarget(), ImmutableMap.of(DetectedVersion.BUILT_IN.getName(), 1))));
    }

}
