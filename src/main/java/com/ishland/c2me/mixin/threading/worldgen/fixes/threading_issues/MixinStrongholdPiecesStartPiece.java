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
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

@Mixin(StrongholdPieces.StartPiece.class)
public class MixinStrongholdPiecesStartPiece {

    @Mutable
    @Shadow @Final public List<StructurePiece> pieces;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.pieces = Collections.synchronizedList(pieces);
    }

}
