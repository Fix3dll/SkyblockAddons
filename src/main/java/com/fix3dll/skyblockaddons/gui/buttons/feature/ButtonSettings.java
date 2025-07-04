package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class ButtonSettings extends ButtonFeature {

    private static final ResourceLocation GEAR = SkyblockAddons.resourceLocation("gui/gear.png");

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSettings(double x, double y, String buttonText, Feature feature) {
        super((int) x, (int) y, Component.literal(buttonText), feature);
        this.width = 15;
        this.height = 15;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        float alphaMultiplier = calculateAlphaMultiplier();
        int color = ARGB.white(this.isHovered ? 1F : alphaMultiplier * 0.7F);
        this.isHovered = isHovered(mouseX, mouseY);

        graphics.blit(RenderType::guiTextured, GEAR, getX(), getY(), 0, 0, width, height, width, height, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (MC.screen instanceof SkyblockAddonsGui gui) {
            main.getUtils().setFadingIn(false);
            if (this.feature == Feature.ENCHANTMENT_LORE_PARSING) {
                MC.setScreen(new EnchantmentSettingsGui(0, gui.getPage(), gui.getTab()));
            } else {
                MC.setScreen(new SettingsGui(this.feature, 1, gui.getPage(), gui.getTab(), EnumUtils.GUIType.MAIN));
            }
        }
    }

}