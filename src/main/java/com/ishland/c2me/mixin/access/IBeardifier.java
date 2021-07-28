package com.ishland.c2me.mixin.access;

import net.minecraft.world.level.levelgen.Beardifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Beardifier.class)
public interface IBeardifier {

    @Invoker
    double invokeBeardifyOrBury(int x, int y, int z);

}
