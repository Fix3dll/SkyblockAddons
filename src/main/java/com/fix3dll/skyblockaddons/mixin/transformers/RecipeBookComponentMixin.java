package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {

    @ModifyReturnValue(method = "getXOrigin", at = @At("RETURN"))
    public int sba$getXOrigin(int original) {
        return original - (SkyblockEquipment.equipmentsInInventory() ? 23 : 0);
    }

}