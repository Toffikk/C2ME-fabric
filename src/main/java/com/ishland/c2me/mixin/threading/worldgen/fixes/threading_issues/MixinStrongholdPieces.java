package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.IStrongholdGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;

@Mixin(StrongholdPieces.class)
public class MixinStrongholdPieces implements IStrongholdGenerator {

    @Shadow @Final private static StrongholdPieces.PieceWeight[] ALL_PIECES;
    @Shadow private static List<StrongholdPieces.PieceWeight> possiblePieces;
    private static final ThreadLocal<Integer> totalWeightThreadLocal = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Class<? extends StrongholdPieces.StrongholdPiece>> activePieceTypeThreadLocal = new ThreadLocal<>();

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;possiblePieces:Ljava/util/List;", opcode = Opcodes.PUTSTATIC))
    private static void redirectAssignList(List<StrongholdPieces.PieceWeight> value) {
        possiblePieces = Collections.synchronizedList(value);
        final List<StrongholdPieces.PieceWeight> pieceDataList = Arrays.asList(ALL_PIECES);
        pieceDataList.forEach(pieceData -> pieceData.placeCount = 0);
        possiblePieces.addAll(pieceDataList);
    }

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD))
    private static void redirectSetGeneratedCount(StrongholdPieces.PieceWeight pieceData, int value) {
        // no-op
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private static <E> boolean redirectListAdd(List<E> list, E e) {
        return false; // no-op
    }

    @Redirect(method = "checkRemainingPieces", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;totalWeight:I", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetTotalWeight(int value) {
        totalWeightThreadLocal.set(value);
    }

    @Redirect(method = {"pickPiece", "checkRemainingPieces"}, at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;totalWeight:I", opcode = Opcodes.GETSTATIC))
    private static int redirectGetTotalWeight() {
        return totalWeightThreadLocal.get();
    }

    @Redirect(method = "pickPiece", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetActivePieceType(Class<? extends StrongholdPieces.StrongholdPiece> value) {
        activePieceTypeThreadLocal.set(value);
    }

    @Redirect(method = "pickPiece", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.GETSTATIC))
    private static Class<? extends StrongholdPieces.StrongholdPiece> redirectGetActivePieceType() {
        return activePieceTypeThreadLocal.get();
    }

    @Override
    public ThreadLocal<Class<? extends StrongholdPieces.StrongholdPiece>> getActivePieceTypeThreadLocal() {
        return activePieceTypeThreadLocal;
    }
}
