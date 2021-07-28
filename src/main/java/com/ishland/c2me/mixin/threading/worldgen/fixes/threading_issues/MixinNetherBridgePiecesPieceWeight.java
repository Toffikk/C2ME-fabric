package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.INetherFortressGeneratorPieceData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;

@Mixin(NetherBridgePieces.PieceWeight.class)
public class MixinNetherBridgePiecesPieceWeight implements INetherFortressGeneratorPieceData {

    private final AtomicInteger generatedCountAtomic = new AtomicInteger();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.GETFIELD))
    private int redirectGetGeneratedCount(NetherBridgePieces.PieceWeight pieceData) {
        return this.generatedCountAtomic.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD), require = 0)
    private void redirectSetGeneratedCount(NetherBridgePieces.PieceWeight pieceData, int value) {
        this.generatedCountAtomic.set(value);
    }

    @Override
    public AtomicInteger getGeneratedCountAtomic() {
        return this.generatedCountAtomic;
    }
}
