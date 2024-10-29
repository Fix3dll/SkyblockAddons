package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;

/**
 * Alternative hooks for the labymod custom gui, to disable specific bars.
 */
public class GuiIngameCustomHook {
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static boolean shouldRenderArmor() {
        return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR);
    }

    public static boolean shouldRenderHealth() {
        return shouldRender(Feature.HIDE_HEALTH_BAR) || (Feature.HIDE_ONLY_OUTSIDE_RIFT.isEnabled() && main.getUtils().isOnRift());
    }

    public static boolean shouldRenderFood() {
        return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR);
    }

    public static boolean shouldRenderMountHealth() {
        return shouldRender(Feature.HIDE_PET_HEALTH_BAR);
    }

    public static boolean shouldRender(Feature feature) {
        if (!main.getUtils().isOnSkyblock()) {
            return true;
        }
        return feature.isDisabled();
    }
}
