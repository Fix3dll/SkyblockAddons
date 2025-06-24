package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import net.minecraft.world.item.ItemStack;

public class ScreenHook {

    public static boolean onRenderTooltip(ItemStack itemStack, int x, int y) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (Feature.DISABLE_EMPTY_GLASS_PANES.isEnabled() && main.getUtils().isBlankGlassPane(itemStack)) {
            return true;
        }

        if (Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS.isDisabled()) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();
            if (inventoryType == InventoryType.ULTRASEQUENCER || inventoryType == InventoryType.CHRONOMATRON) {
                return true;
            }
        }

        return ContainerPreviewManager.onRenderTooltip(itemStack, x, y);
    }

}