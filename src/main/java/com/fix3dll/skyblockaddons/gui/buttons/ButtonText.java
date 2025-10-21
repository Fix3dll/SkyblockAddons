package com.fix3dll.skyblockaddons.gui.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ButtonText extends SkyblockAddonsButton {

    private final boolean centered;
    private final int color;

    public ButtonText(int x, int y, String text, boolean centered, int color) {
        super(x, y, Component.literal(text));
        this.centered = centered;
        this.color = color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();

        if (centered) {
            x -= MC.font.width(getMessage()) / 2;
        }

        graphics.drawString(MC.font, getMessage(), x, y, color, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

}