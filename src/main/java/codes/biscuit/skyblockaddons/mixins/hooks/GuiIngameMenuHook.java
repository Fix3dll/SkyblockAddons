package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.gui.GuiButton;

import java.util.List;

public class GuiIngameMenuHook {

    public static void addMenuButtons(List<GuiButton> buttonList, int width, int height) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && Feature.SBA_BUTTON_IN_PAUSE_MENU.isEnabled()) {
            buttonList.add(new GuiButton(53, width - 120 - 5, height - 20 - 5, 120, 20, "SkyblockAddons"));
        }
    }

    public static void onButtonClick() {
        SkyblockAddons skyblockAddons = SkyblockAddons.getInstance();
        skyblockAddons.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
    }
}
