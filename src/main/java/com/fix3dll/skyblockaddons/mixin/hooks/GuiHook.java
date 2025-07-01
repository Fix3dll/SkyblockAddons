package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;

public class GuiHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static boolean renderHearts = true;
    public static boolean renderArmor = true;
    public static boolean renderFood = true;
    public static boolean renderVehicleHealth = true;
    public static boolean renderEffectsHud = true;

    public static boolean isOnSkyblock() {
        return main.isFullyInitialized() && main.getUtils().isOnSkyblock();
    }

    public static boolean isHideOnlyOutsideRiftEnabled() {
        return Feature.HIDE_HEALTH_BAR.isDisabled()
                || (Feature.HIDE_HEALTH_BAR.isEnabled(FeatureSetting.HIDE_ONLY_OUTSIDE_RIFT)
                && main.isFullyInitialized()
                && main.getUtils().isOnRift());
    }

}