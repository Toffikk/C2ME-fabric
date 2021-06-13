package org.yatopiamc.c2me.mixin.optimization.worldgen.threadsafe_weightedlist;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

@Mixin(ShufflingList.class)
public class MixinWeightedList<U> {

    @Shadow @Final public List<ShufflingList.WeightedEntry<U>> entries;

    /**
     * @author ishland
     * @reason create new instance on shuffling
     */
    @Overwrite
    public ShufflingList<U> shuffle() {
        // TODO [VanillaCopy]
        final ShufflingList<U> newList = new ShufflingList<>(entries); // C2ME - use new instance
        newList.entries.forEach((entry) -> { // C2ME - use new instance
            entry.setRandom(new Random().nextFloat());
        });
        newList.entries.sort(Comparator.comparingDouble((object) -> { // C2ME - use new instance
            return ((ShufflingList.WeightedEntry)object).getRandWeight();
        }));
        return newList; // C2ME - use new instance
    }

}
