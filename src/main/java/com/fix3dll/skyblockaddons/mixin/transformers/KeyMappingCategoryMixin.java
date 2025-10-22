package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.Category.class)
public class KeyMappingCategoryMixin {

    @Unique
    private static final Component SBA_CATEGORY_LABEL = Component.literal(SkyblockAddons.METADATA.getName());

    @Inject(method = "label", at = @At("HEAD"), cancellable = true)
    public void sba$label(CallbackInfoReturnable<Component> cir) {
        if ((KeyMapping.Category) (Object) this == SkyblockAddons.CATEGORY) {
            cir.setReturnValue(SBA_CATEGORY_LABEL);
        }
    }

}