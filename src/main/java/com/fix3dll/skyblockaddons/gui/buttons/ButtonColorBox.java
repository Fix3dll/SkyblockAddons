package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
@Getter
public class ButtonColorBox extends SkyblockAddonsButton {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    private final ColorCode color;

    public ButtonColorBox(int x, int y, ColorCode color) {
        super(x, y, Component.empty());
        this.width = WIDTH;
        this.height = HEIGHT;
        this.color = color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);
        drawColorRect(graphics, getX(), getY(), getX() + width, getY() + height, this.isHovered ? color.getColor() : color.getColor(127));
    }

    public static void drawColorRect(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        boolean isChromaColor = color == ColorCode.CHROMA.getColor(ARGB.alpha(color));

        if (isChromaColor) {
            if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                graphics.fill(DrawUtils.CHROMA_STANDARD, left, top, right, bottom, color);
            } else {
                drawChromaRect(graphics, left, top, right, bottom, ARGB.alpha(color));
            }
        } else {
            graphics.fill(left, top, right, bottom, color);
        }
    }

    public static void drawChromaRect(GuiGraphics graphics, int left, int top, int right, int bottom, int alpha) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        int colorLB = ManualChromaManager.getChromaColor(left, bottom, 1);
        int colorRB = ManualChromaManager.getChromaColor(right, bottom, 1);
        int colorLT = ManualChromaManager.getChromaColor(left, top, 1);
        int colorRT = ManualChromaManager.getChromaColor(right, top, 1);
        int colorMM = ManualChromaManager.getChromaColor(Math.floorDiv((right+left), 2), Math.floorDiv((top+bottom), 2), 1);

        int finalRight = right, finalBottom = bottom, finalLeft = left, finalTop = top;
        graphics.drawSpecial(source -> {
            VertexConsumer vertex = source.getBuffer(RenderType.gui());
            // First triangle
            vertex.addVertex(finalRight, finalBottom, 0.0F).setColor(ARGB.color(alpha, colorRB));
            vertex.addVertex(Math.floorDiv((finalRight + finalLeft), 2), Math.floorDiv((finalTop + finalBottom), 2), 0.0F).setColor(ARGB.color(alpha, colorMM));
            vertex.addVertex(finalLeft, finalTop, 0.0F).setColor(ARGB.color(alpha, colorLT));
            vertex.addVertex(finalLeft, finalBottom, 0.0F).setColor(ARGB.color(alpha, colorLB));
            // 2nd triangle
            vertex.addVertex(finalRight, finalBottom, 0.0F).setColor(ARGB.color(alpha, colorRB));
            vertex.addVertex(finalRight, finalTop, 0.0F).setColor(ARGB.color(alpha, colorRT));
            vertex.addVertex(finalLeft, finalTop, 0.0F).setColor(ARGB.color(alpha, colorLT));
            vertex.addVertex(Math.floorDiv((finalRight + finalLeft), 2), Math.floorDiv((finalTop + finalBottom), 2), 0.0F).setColor(ARGB.color(alpha, colorMM));
        });
    }
}