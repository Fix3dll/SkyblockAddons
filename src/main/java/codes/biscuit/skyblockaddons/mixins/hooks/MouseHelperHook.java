package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import org.lwjgl.input.Mouse;

public class MouseHelperHook {

    public static void ungrabMouseCursor(int new_x, int new_y) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isDisabled() || main.getPlayerListener().shouldResetMouse()) {
            Mouse.setCursorPosition(new_x, new_y);
            Mouse.setGrabbed(false);
        }
    }
}
