package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import net.minecraft.world.level.levelgen.structure.StrongholdPieces;

public interface IStrongholdGenerator {

    ThreadLocal<Class<? extends StrongholdPieces.StrongholdPiece>> getActivePieceTypeThreadLocal();

    class Holder {
        @SuppressWarnings({"InstantiationOfUtilityClass", "ConstantConditions"})
        public static final IStrongholdGenerator INSTANCE = (IStrongholdGenerator) new StrongholdPieces();
    }

}
