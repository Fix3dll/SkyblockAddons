package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class ButtonArrow extends SkyblockAddonsButton {

    @Getter private final ArrowType arrowType;
    private final boolean max;

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonArrow(double x, double y, ArrowType arrowType, boolean max) {
        super((int) x, (int) y, Component.empty());
        this.width = 30;
        this.height = 30;
        this.arrowType = arrowType;
        this.max = max;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        float alphaMultiplier = calculateAlphaMultiplier();
        this.isHovered = isHovered(mouseX, mouseY);

        int color;
        if (max) {
            color = ARGB.colorFromFloat(alphaMultiplier * 0.5F, 0.5F, 0.5F, 0.5F);
        } else {
            color = ARGB.white(this.isHovered ? 1F : alphaMultiplier * 0.7F);
        }

        graphics.blit(RenderType::guiTextured, arrowType.identifier, getX(), getY(), 0, 0, width, height, width, height, color);
//            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.max) {
            main.getUtils().setFadingIn(false);

            if (MC.screen instanceof SkyblockAddonsGui gui) {
                if (gui.getTab() == EnumUtils.GuiTab.GENERAL_SETTINGS) gui.setCancelClose(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                MC.setScreen(new SkyblockAddonsGui(page, gui.getTab()));

                if (gui.getTab() == EnumUtils.GuiTab.GENERAL_SETTINGS) gui.setCancelClose(false);

            } else if (MC.screen instanceof EnchantmentSettingsGui gui) {
                gui.setClosingGui(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                MC.setScreen(new EnchantmentSettingsGui(page, gui.getLastPage(), gui.getLastTab()));

            } else if (MC.screen instanceof SettingsGui gui) {
                gui.setClosingGui(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                MC.setScreen(new SettingsGui(gui.getFeature(), page, gui.getLastPage(), gui.getLastTab(), gui.getLastGUI()));
            }
        }
    }

    public enum ArrowType {
        LEFT("gui/arrowleft.png"),
        RIGHT("gui/arrowright.png");

        final ResourceLocation identifier;

        ArrowType(String path) {
            this.identifier = ResourceLocation.fromNamespaceAndPath(SkyblockAddons.MOD_ID, path);

        }
    }
}