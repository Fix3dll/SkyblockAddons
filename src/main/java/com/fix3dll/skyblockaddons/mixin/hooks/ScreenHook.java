package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ScreenHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static IslandWarpGui islandWarpGui = null;

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

    /**
     * @return true if ContainerScreen rendering should be bypassed
     */
    public static boolean drawScreenIslands(Screen instance, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (MC.player == null || !main.getUtils().isOnSkyblock()) {
            return false; // don't draw any overlays outside SkyBlock
        }

        if (Feature.FANCY_WARP_MENU.isEnabled()) {
            Component titleComponent = instance.getTitle();
            if (titleComponent == null) return false;
            String title = titleComponent.getString();
            if (title.equals("Fast Travel")) {
                if (islandWarpGui == null) {
                    islandWarpGui = new IslandWarpGui();
                    islandWarpGui.init(MC, MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight());
                }

                try {
                    islandWarpGui.render(graphics, mouseX, mouseY, partialTick);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

                return true;
            } else {
                islandWarpGui = null;
            }
        } else {
            islandWarpGui = null;
        }

        return false;
    }

}