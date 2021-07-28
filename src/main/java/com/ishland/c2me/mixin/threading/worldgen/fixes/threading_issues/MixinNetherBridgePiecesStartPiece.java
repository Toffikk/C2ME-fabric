package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.INetherFortressGeneratorPieceData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;

@Mixin(NetherBridgePieces.StartPiece.class)
public class MixinNetherBridgePiecesStartPiece {

    @Shadow public List<NetherBridgePieces.PieceWeight> bridgePieces;
    @Shadow public List<NetherBridgePieces.PieceWeight> corridorPieces;

    @Redirect(method = "<init>(Ljava/util/Random;II)V", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD))
    private void redirectSetPieceDataGeneratedCount(NetherBridgePieces.PieceWeight pieceData, int value) {
        ((INetherFortressGeneratorPieceData) pieceData).getGeneratedCountAtomic().set(value);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.bridgePieces = Collections.synchronizedList(this.bridgePieces);
        this.corridorPieces = Collections.synchronizedList(this.corridorPieces);
    }
}
