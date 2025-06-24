package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.utils.MathUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class ButtonSlider extends SkyblockAddonsButton {

    private final float min;
    private final float max;
    private final float step;
    private final UpdateCallback<Float> sliderCallback;
    private String prefix = "";

    private boolean dragging;
    private float normalizedValue;
    private double previousDoubleMouseX = -1D;

    public ButtonSlider(double x, double y, int width, int height, float value, float min, float max, float step, UpdateCallback<Float> sliderCallback) {
        super((int) x, (int) y, Component.literal(TextUtils.roundForString(value, 2)));
        this.width = width;
        this.height = height;
        this.sliderCallback = sliderCallback;
        this.min = min;
        this.max = max;
        this.step = step;
        this.normalizedValue = MathUtils.normalizeSliderValue(value, min, max, step);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);
        int boxAlpha = this.isHovered ? 170 : 100;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, main.getUtils().getDefaultColor(boxAlpha));
        this.onDrag(graphics, mouseX, mouseY);
        int i = this.active ? 16777215 : 10526880;
        renderScrollingString(graphics, MC.font, 2, i | Mth.ceil(alpha * 255.0F) << 24);
    }

    protected void onDrag(GuiGraphics graphics, double mouseX, double mouseY) {
        if (this.visible) {
            double doubleMouseX = (MC.mouseHandler.xpos() * (double)MC.getWindow().getGuiScaledWidth() / (double)MC.getWindow().getWidth());

            if (this.dragging && previousDoubleMouseX != doubleMouseX) {
                previousDoubleMouseX = doubleMouseX;
                this.normalizedValue = (float) ((doubleMouseX - (getX() + 4)) / (float) (width - 8));
                this.normalizedValue = Mth.clamp(normalizedValue, 0.0F, 1.0F);
                onUpdate();
            }

            graphics.blitSprite(
                    RenderType::guiTextured,
                    SPRITES.get(this.active, this.isHovered),
                    getX() + (int) (this.normalizedValue * (float) (this.width - 8)) + 1,
                    this.getY(),
                    6,
                    this.getHeight(),
                    ARGB.white(this.alpha)
            );
            //graphics.fill(getX() + (int) (this.normalizedValue * (float) (this.width - 8)) + 1, getY(), getX() + (int) (this.normalizedValue * (float) (this.width - 8))+7, getY() + this.height, ColorCode.GRAY.getColor());
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.dragging = false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isHovered) {
            this.normalizedValue = (float) (mouseX - (getX() + 4)) / (float) (this.width - 8);
            this.normalizedValue = Mth.clamp(this.normalizedValue, 0.0F, 1.0F);
            this.dragging = true;
            onUpdate();
        }
    }

    public ButtonSlider setPrefix(String text) {
        prefix = text;
        this.updateDisplayString();
        return this;
    }

    private void onUpdate() {
        sliderCallback.onUpdate(denormalize());
        this.updateDisplayString();
    }

    private void updateDisplayString() {
        setMessage(Component.literal(prefix + TextUtils.roundForString(denormalize(), 2)));
    }

    public float denormalize() {
        return MathUtils.denormalizeSliderValue(normalizedValue, min, max, step);
    }
}