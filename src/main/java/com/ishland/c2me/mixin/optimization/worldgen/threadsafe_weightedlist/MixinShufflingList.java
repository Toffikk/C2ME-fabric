package com.ishland.c2me.mixin.optimization.worldgen.threadsafe_weightedlist;

import com.ishland.c2me.common.optimization.worldgen.threadsafe_weightedlist.IShufflingList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

@Mixin(ShufflingList.class)
public class MixinShufflingList<U> implements IShufflingList<U> {

    @Shadow @Final public List<ShufflingList.WeightedEntry<U>> entries;

    @Shadow @Final private Random random;

    /**
     * @author ishland
     * @reason create new instance on shuffling
     */
    @Overwrite
    public ShufflingList<U> shuffle() {
        // TODO [VanillaCopy]
        final ShufflingList<U> newList = new ShufflingList<>(entries); // C2ME - use new instance
        final Random random = new Random(); // C2ME - use new instance
        newList.entries.forEach((entry) -> { // C2ME - use new instance
            entry.setRandom(random.nextFloat());
        });
        newList.entries.sort(Comparator.comparingDouble((object) -> { // C2ME - use new instance
            return ((ShufflingList.WeightedEntry)object).getRandWeight();
        }));
        return newList; // C2ME - use new instance
    }

    @Override
    public ShufflingList<U> shuffleVanilla() {
        // TODO [VanillaCopy]
        this.entries.forEach((entry) -> {
            entry.setRandom(this.random.nextFloat());
        });
        this.entries.sort(Comparator.comparingDouble(ShufflingList.WeightedEntry::getRandWeight));
        return (ShufflingList<U> ) (Object) this;
    }
}
