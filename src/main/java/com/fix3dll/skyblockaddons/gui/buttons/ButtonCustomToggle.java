package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class ButtonCustomToggle extends SkyblockAddonsButton {

    private static final ResourceLocation TOGGLE_INSIDE_CIRCLE = SkyblockAddons.resourceLocation("gui/toggleinsidecircle.png");
    private static final ResourceLocation TOGGLE_BORDER = SkyblockAddons.resourceLocation("gui/toggleborder.png");
    private static final ResourceLocation TOGGLE_INSIDE_BACKGROUND = SkyblockAddons.resourceLocation("gui/toggleinsidebackground.png");
    private static final int animationSlideTime = 150;

    private final int circlePaddingLeft;
    private final int animationSlideDistance;

    private long animationButtonClicked = -1;

    private final Supplier<Boolean> enabledSupplier;
    private final Runnable onClickRunnable;

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonCustomToggle(double x, double y, int height, Supplier<Boolean> enabledSupplier, Runnable onClickRunnable) {
        super((int) x, (int) y, Component.empty());
        this.width = (int)Math.round(height * 2.07);
        this.height = height;
        this.enabledSupplier = enabledSupplier;
        this.onClickRunnable = onClickRunnable;

        circlePaddingLeft = height / 3;
        animationSlideDistance = Math.round(height * 0.8F);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = ARGB.color(255, 30, 37, 46);
        graphics.blit(RenderType::guiTextured, TOGGLE_BORDER, getX(), getY(), 0, 0, width, height, width, height, color);

        boolean enabled = enabledSupplier.get();
        if (enabled) {
            color = ARGB.color(255, 36, 255, 98); // Green
        } else {
            color = ARGB.color(255, 222, 68, 76); // Red
        }

        graphics.blit(RenderType::guiTextured, TOGGLE_INSIDE_BACKGROUND, getX(), getY(), 0, 0, width, height, width, height, color);

        int startingX = getButtonStartingX(enabled);
        int slideAnimationOffset = 0;

        if (animationButtonClicked != -1) {
            startingX = getButtonStartingX(!enabled); // They toggled so start from the opposite side.

            int timeSinceOpen = (int)(System.currentTimeMillis() - animationButtonClicked);
            int animationTime = animationSlideTime;
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime;
            }

            slideAnimationOffset = animationSlideDistance * timeSinceOpen/animationTime;
        }

        startingX += enabled ? slideAnimationOffset : -slideAnimationOffset;
        color = ARGB.white(1F);
        int circleSize = Math.round(height * 0.6F); // 60% of the height.
        int y = Math.round(getY() + (height * 0.2F)); // 20% OF the height.

        graphics.blit(RenderType::guiTextured, TOGGLE_INSIDE_CIRCLE, startingX,y, 0, 0, circleSize, circleSize, circleSize, circleSize, color);
    }

    /**
     * The inside circle starts at either the left or right
     * side depending on whether this button is enabled.
     * This returns that x position.
     */
    private int getButtonStartingX(boolean enabled) {
        if (!enabled)  {
            return getX() + circlePaddingLeft;
        } else {
            return getButtonStartingX(false) + animationSlideDistance;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.animationButtonClicked = System.currentTimeMillis();
        this.onClickRunnable.run();
    }

}