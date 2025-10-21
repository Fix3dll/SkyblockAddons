package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.render.state.ButtonColorBoxRenderState;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
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
                graphics.guiRenderState.submitGuiElement(
                        new ButtonColorBoxRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), graphics.pose(), left, top, right, bottom, ARGB.alpha(color), graphics.scissorStack.peek())
                );
            }
        } else {
            graphics.fill(left, top, right, bottom, color);
        }
    }

}