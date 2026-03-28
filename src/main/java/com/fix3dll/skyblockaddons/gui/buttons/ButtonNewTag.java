package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ButtonNewTag extends SkyblockAddonsButton {

    public ButtonNewTag(int x, int y) {
        super(x, y, Component.literal("NEW"));

        width = 25;
        height = 11;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, ColorCode.RED.getColor());
        graphics.text(MC.font, getMessage(), getX() + 4, getY() + 2, ColorCode.WHITE.getColor(), false);
    }

    @Override
    public void playDownSound(@NonNull SoundManager soundManager) {
    }

}