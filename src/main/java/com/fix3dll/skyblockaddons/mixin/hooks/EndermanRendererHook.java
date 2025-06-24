package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import net.minecraft.resources.ResourceLocation;

public class EndermanRendererHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation BLANK_ENDERMAN_TEXTURE = ResourceLocation.fromNamespaceAndPath("skyblockaddons", "blankenderman.png");

    public static int getEndermanColor() {
        if (main.getUtils().isOnSkyblock()
                && LocationUtils.isOnZealotSpawnLocation()
                && Feature.CHANGE_ZEALOT_COLOR.isEnabled()) {
            return Feature.CHANGE_ZEALOT_COLOR.getColor();
        }
        return -1;
    }

    public static ResourceLocation getEndermanTexture() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && Feature.CHANGE_ZEALOT_COLOR.isEnabled() && LocationUtils.isOnZealotSpawnLocation()) {
            return BLANK_ENDERMAN_TEXTURE;
        }
        return null;
    }

}