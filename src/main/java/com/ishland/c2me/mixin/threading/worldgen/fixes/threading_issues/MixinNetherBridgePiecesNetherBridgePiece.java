package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.INetherFortressGeneratorPieceData;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherBridgePieces.NetherBridgePiece.class)
public class MixinNetherBridgePiecesNetherBridgePiece {

    @Redirect(method = "checkRemainingPieces", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.GETFIELD))
    private int redirectGetPieceDataGeneratedCount(NetherBridgePieces.PieceWeight pieceData) {
        return ((INetherFortressGeneratorPieceData) pieceData).getGeneratedCountAtomic().get();
    }

    @Redirect(method = "pickPiece", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD))
    private void redirectIncrementPieceDataGeneratedCount(NetherBridgePieces.PieceWeight pieceData, int value) { // TODO Check when updating minecraft version
        ((INetherFortressGeneratorPieceData) pieceData).getGeneratedCountAtomic().incrementAndGet();
    }

}
