package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.utils.MathUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

public class ButtonSlider extends SkyblockAddonsButton {

    private final float min;
    private final float max;
    private final float step;
    private final boolean stepHasDecimal;
    private final UpdateCallback<Float> sliderCallback;
    private String prefix = "";
    private String suffix = "";

    private boolean dragging;
    private float normalizedValue;
    private double previousDoubleMouseX = -1D;

    public ButtonSlider(double x, double y, int width, int height, float value, float min, float max, float step, UpdateCallback<Float> sliderCallback) {
        super((int) x, (int) y, Component.literal(TextUtils.roundForString(value, step != ((int) step) ? 2 : 0)));
        this.width = width;
        this.height = height;
        this.sliderCallback = sliderCallback;
        this.min = min;
        this.max = max;
        this.step = step;
        this.stepHasDecimal = step != ((int) step);
        this.normalizedValue = MathUtils.normalizeSliderValue(value, min, max, step);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);
        int boxAlpha = this.isHovered ? 170 : 100;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, main.getUtils().getDefaultColor(boxAlpha));
        this.onDrag(graphics, mouseX, mouseY);
        extractScrollingStringOverContents(
                graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE),
                this.message,
                2
        );
    }

    protected void onDrag(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        if (this.visible) {
            double doubleMouseX = MC.mouseHandler.getScaledXPos(MC.getWindow());

            if (this.dragging && previousDoubleMouseX != doubleMouseX) {
                previousDoubleMouseX = doubleMouseX;
                this.normalizedValue = (float) ((doubleMouseX - (getX() + 4)) / (float) (width - 8));
                this.normalizedValue = Mth.clamp(normalizedValue, 0.0F, 1.0F);
                this.updateDisplayString();
            }

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    SPRITES.get(this.active, this.isHovered),
                    getX() + (int) (this.normalizedValue * (float) (this.width - 8)) + 1,
                    this.getY(),
                    6,
                    this.getHeight(),
                    ARGB.white(this.alpha)
            );
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        this.dragging = false;
        sliderCallback.onUpdate(denormalize());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        this.normalizedValue = (float) (event.x() - (getX() + 4)) / (float) (this.width - 8);
        this.normalizedValue = Mth.clamp(this.normalizedValue, 0.0F, 1.0F);
        this.dragging = true;
        this.updateDisplayString();
    }

    public ButtonSlider setPrefix(String text) {
        prefix = text;
        this.updateDisplayString();
        return this;
    }

    public ButtonSlider setSuffix(String text) {
        suffix = text;
        this.updateDisplayString();
        return this;
    }

    private void updateDisplayString() {
        setMessage(Component.literal(prefix + TextUtils.roundForString(denormalize(), stepHasDecimal ? 2 : 0) + suffix));
    }

    public float denormalize() {
        return MathUtils.denormalizeSliderValue(normalizedValue, min, max, step);
    }

}