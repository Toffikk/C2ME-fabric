package com.ishland.c2me.tests.testmod.mixin;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.network.ServerConnectionListener;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @Shadow
    @Final
    static Logger LOGGER;

    /**
     * @author ishland
     * @reason stop watchdog
     */
    @Overwrite
    public long getMaxTickTime() {
        return 0;
    }

    @Redirect(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerNetworkIo;bind(Ljava/net/InetAddress;I)V"))
    private void redirectNetworkBind(ServerConnectionListener serverNetworkIo, InetAddress address, int port) {
        LOGGER.info("Not actually binding ports");
    }

}
