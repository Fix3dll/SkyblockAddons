package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.utils.EnumUtils;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

@Getter
public class ButtonSocial extends SkyblockAddonsButton {

    private final EnumUtils.Social social;

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonSocial(double x, double y, EnumUtils.Social social) {
        super((int) x, (int) y, Component.empty());
        this.width = 20;
        this.height = 20;
        this.social = social;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alphaMultiplier = calculateAlphaMultiplier();
        this.isHovered = isHovered(mouseX, mouseY);
        int color = ARGB.white(alphaMultiplier * (this.isHovered ? 1F : 0.7F));

        graphics.blit(RenderType::guiTextured, social.getResourceLocation(), getX(), getY(), 0, 0, width, height, width, height, color);
//        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, width, height, width, height, true);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Util.getPlatform().openUri(social.getUrl());
    }
}