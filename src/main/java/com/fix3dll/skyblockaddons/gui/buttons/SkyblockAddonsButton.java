package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3x2fStack;

public abstract class SkyblockAddonsButton extends AbstractWidget {

    protected static final SkyblockAddons main = SkyblockAddons.getInstance();
    protected static final Minecraft MC = Minecraft.getInstance();
    protected static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/button"),
            ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );
    private static final int FADE_MILLIS = 150;

    /** Used to calculate the transparency when fading in. */
    private final long timeOpened = System.currentTimeMillis();

    /** A value to specify the drawing order. */
    public int priority;

    protected float scale = 1.0F;

    public SkyblockAddonsButton(int x, int y, Component buttonText) {
        this(x, y, 200, 20, buttonText);
    }

    public SkyblockAddonsButton(int x, int y, int width, int height, Component buttonText) {
        super(x, y, width, height, buttonText);
    }

    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return isHovered(this.getX(), this.getY(), mouseX, mouseY, scale);
    }

    public boolean isHovered(float x, float y, double mouseX, double mouseY, float scale) {
        return this.active && this.visible
                && mouseX >= x * scale && mouseX < x * scale + this.width * scale
                && mouseY >= y * scale && mouseY < y * scale + this.height * scale;
    }

    public float calculateAlphaMultiplier() {
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            if (timeSinceOpen <= FADE_MILLIS) {
                return (float) timeSinceOpen / FADE_MILLIS;
            }
        }
        return 1.0F;
    }

    public void drawButtonBoxAndText(GuiGraphics graphics, int boxColor, float scale, int fontColor) {
        drawButtonBoxAndText(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), boxColor, scale, fontColor);
    }

    public void drawButtonBoxAndText(GuiGraphics graphics, Component message, int x, int y, int width, int height, int boxColor, float scale, int fontColor) {
        ButtonColorBox.drawColorRect(graphics, x, y, x + width, y + height, boxColor);
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale);
        //noinspection IntegerDivisionInFloatingPointContext
        DrawUtils.drawCenteredText(
                graphics,
                message.getString(),
                ((x + width / 2) / scale),
                ((y + (height - (8 * scale)) / 2) / scale),
                fontColor
        );
        poseStack.popMatrix();
    }

}