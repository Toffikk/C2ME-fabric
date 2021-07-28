package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;

@Mixin(MineShaftPieces.MineShaftRoom.class)
public class MixinMineshaftPiecesMineshaftRoom {

    @Mutable
    @Shadow @Final private List<BoundingBox> entrances;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.entrances = Collections.synchronizedList(this.entrances);
    }

}
