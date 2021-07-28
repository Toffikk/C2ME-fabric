package com.ishland.c2me.tests.testmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.util.Properties;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.levelgen.WorldGenSettings;

@Mixin(DedicatedServerProperties.class)
public class MixinDedicatedServerProperties {

    @Redirect(method = "method_37371", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/GeneratorOptions;fromProperties(Lnet/minecraft/util/registry/DynamicRegistryManager;Ljava/util/Properties;)Lnet/minecraft/world/gen/GeneratorOptions;"))
    private WorldGenSettings redirectGeneratorOptions(RegistryAccess registryManager, Properties properties) throws IOException {
        final Properties properties1 = new Properties();
        properties1.put("level-seed", "c2metest");
        final WorldGenSettings generatorOptions = WorldGenSettings.create(registryManager, properties1);
        properties1.store(System.err, "C2ME Test Generated Generator Settings");
        return generatorOptions;
    }

}
