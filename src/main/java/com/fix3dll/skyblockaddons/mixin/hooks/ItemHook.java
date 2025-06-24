package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ItemHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static boolean isItemDamaged(ItemStack stack) {
        if (main.getUtils().isOnSkyblock() && Feature.SHOW_ITEM_COOLDOWNS.isEnabled() && CooldownManager.isOnCooldown(stack)) {
            return true;
        }
        return stack.isDamaged();
    }

    public static void getBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (main.getUtils().isOnSkyblock() && Feature.SHOW_ITEM_COOLDOWNS.isEnabled() && CooldownManager.isOnCooldown(stack)) {
            cir.setReturnValue(
                    Mth.clamp(Math.round((float) CooldownManager.getRemainingCooldownPercent(stack) * 13), 0, 13)
            );
        }
    }

    public static void getBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (main.getUtils().isOnSkyblock() && Feature.SHOW_ITEM_COOLDOWNS.isEnabled() && CooldownManager.isOnCooldown(stack)) {
            float f = Math.max(0.0F, (float) CooldownManager.getRemainingCooldownPercent(stack));
            cir.setReturnValue(
                    Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F)
            );
        }
    }

}