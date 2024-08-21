package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;

public class ModelEndermanHook {

    public static void setEndermanColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && LocationUtils.isOnZealotSpawnLocation()
                && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            int color = main.getConfigValues().getColor(Feature.CHANGE_ZEALOT_COLOR);
            ColorUtils.bindColor(color);
        }
    }
}
