package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackInventoryManager;
import net.minecraft.util.ARGB;

public class ContainerScreenHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static int color() {
        if (!main.getUtils().isOnSkyblock()) {
            return -1;
        }

        if (Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            if (main.getInventoryUtils().getInventoryType() == InventoryType.STORAGE_BACKPACK) {
                int pageNum = main.getInventoryUtils().getInventoryPageNum();
                if (BackpackInventoryManager.getBackpackColor().containsKey(pageNum)) {
                    BackpackColor color = BackpackInventoryManager.getBackpackColor().get(pageNum);
                    return ARGB.colorFromFloat(1.0F, color.getR(), color.getG(), color.getB());
                }
            }
        }
        return -1;
    }

}