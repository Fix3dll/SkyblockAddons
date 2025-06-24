package com.fix3dll.skyblockaddons.features.tablist;

import net.minecraft.client.Minecraft;

public record TabLine(String text, TabStringType type) {

    public int getWidth() {

        int width = Minecraft.getInstance().font.width(text);

        if (type == TabStringType.PLAYER) {
            width += 8 + 2; // Player head
        }

        if (type == TabStringType.TEXT) {
            width += 4; // Space is 4
        }

        return width;
    }

}