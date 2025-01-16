package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class ButtonCustomToggle extends SkyblockAddonsButton {

    private static final ResourceLocation TOGGLE_INSIDE_CIRCLE = new ResourceLocation("skyblockaddons", "gui/toggleinsidecircle.png");
    private static final ResourceLocation TOGGLE_BORDER = new ResourceLocation("skyblockaddons", "gui/toggleborder.png");
    private static final ResourceLocation TOGGLE_INSIDE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/toggleinsidebackground.png");
    private static final int animationSlideTime = 150;

    private final int circlePaddingLeft;
    private final int animationSlideDistance;

    private long animationButtonClicked = -1;

    private final Supplier<Boolean> enabledSupplier;
    private final Runnable onClickRunnable;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonCustomToggle(double x, double y, int height, Supplier<Boolean> enabledSupplier, Runnable onClickRunnable) {
        super(0, (int)x, (int)y, "");
        this.width = (int)Math.round(height*2.07);
        this.height = height;
        this.enabledSupplier = enabledSupplier;
        this.onClickRunnable = onClickRunnable;

        circlePaddingLeft = height/3;
        animationSlideDistance = Math.round(height*0.8F);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = isHovered(mouseX, mouseY);

        GlStateManager.enableBlend();
        ColorUtils.bindColor(0xFF1e252e);
        mc.getTextureManager().bindTexture(TOGGLE_BORDER);
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height, true);

        boolean enabled = enabledSupplier.get();
        if (enabled) {
            ColorUtils.bindColor(36, 255, 98, 255); // Green
        } else {
            ColorUtils.bindColor(222, 68, 76, 255); // Red
        }

        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_BACKGROUND);
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height, true);

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

        GlStateManager.color(1,1,1,1);
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE);
        int circleSize = Math.round(height*0.6F); // 60% of the height.
        int y = Math.round(yPosition+(this.height*0.2F)); // 20% OF the height.
        DrawUtils.drawModalRectWithCustomSizedTexture(startingX, y,0,0, circleSize, circleSize, circleSize, circleSize, true);
        GlStateManager.disableBlend();
    }

    /**
     * The inside circle starts at either the left or right
     * side depending on whether this button is enabled.
     * This returns that x position.
     */
    private int getButtonStartingX(boolean enabled) {
        if (!enabled)  {
            return xPosition + circlePaddingLeft;
        } else {
            return getButtonStartingX(false) + animationSlideDistance;
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = isHovered(mouseX, mouseY);
        if (pressed) {
            this.animationButtonClicked = System.currentTimeMillis();
            onClickRunnable.run();
        }
        return pressed;
    }
}
