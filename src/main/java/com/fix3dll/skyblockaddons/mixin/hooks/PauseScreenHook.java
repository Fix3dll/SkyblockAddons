package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

public class PauseScreenHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static void addMenuButtons(GridLayout.RowHelper rowHelper) {
        if (main.getUtils().isOnSkyblock() && Feature.SBA_BUTTON_IN_PAUSE_MENU.isEnabled()) {
            rowHelper.addChild(Button.builder(Component.literal(SkyblockAddons.METADATA.getName()), button ->
                SkyblockAddons.getInstance().getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN)
            ).width(204).build(), 2);
        }
    }

}