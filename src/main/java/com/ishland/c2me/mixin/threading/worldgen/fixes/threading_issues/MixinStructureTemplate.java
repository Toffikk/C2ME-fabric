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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Mixin(StructureTemplate.class)
public class MixinStructureTemplate {

    @Mutable
    @Shadow
    @Final
    private List<StructureTemplate.Palette> blockInfoLists;

    @Mutable
    @Shadow
    @Final
    private List<StructureTemplate.StructureEntityInfo> entities;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.blockInfoLists = Collections.synchronizedList(blockInfoLists);
        this.entities = Collections.synchronizedList(entities);
    }

}
