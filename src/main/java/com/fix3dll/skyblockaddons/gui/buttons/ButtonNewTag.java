package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ButtonNewTag extends SkyblockAddonsButton {

    public ButtonNewTag(int x, int y) {
        super(x, y, Component.literal("NEW"));

        width = 25;
        height = 11;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, ColorCode.RED.getColor());
        graphics.drawString(MC.font, getMessage(), getX() + 4, getY() + 2, ColorCode.WHITE.getColor(), false);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

}