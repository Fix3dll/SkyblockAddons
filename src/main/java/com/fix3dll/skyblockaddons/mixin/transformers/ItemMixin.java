package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ItemHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void sba$isBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemHook.isItemDamaged(stack));
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void sba$getBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ItemHook.getBarWidth(stack, cir);
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void sba$getBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ItemHook.getBarColor(stack, cir);
    }

}