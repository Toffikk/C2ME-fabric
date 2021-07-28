package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import com.ishland.c2me.mixin.access.IBeardifier;
import net.minecraft.world.level.levelgen.Beardifier;

public class ThreadLocalBeardifier extends Beardifier {

    private final ThreadLocal<Beardifier> beardifierThreadLocal;

    public ThreadLocalBeardifier() {
        this.beardifierThreadLocal = ThreadLocal.withInitial(Beardifier::new);
    }

    @Override
    protected double beardifyOrBury(int x, int y, int z) {
        return ((IBeardifier) beardifierThreadLocal.get()).invokeBeardifyOrBury(x, y, z);
    }
}
