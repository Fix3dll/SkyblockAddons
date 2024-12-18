package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonArrow extends SkyblockAddonsButton {

    @Getter private final ArrowType arrowType;
    private final boolean max;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonArrow(double x, double y, ArrowType arrowType, boolean max) {
        super(0, (int)x, (int)y, null);
        this.width = 30;
        this.height = 30;
        this.arrowType = arrowType;
        this.max = max;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = calculateAlphaMultiplier();
            hovered = isHovered(mouseX, mouseY);
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(arrowType.resourceLocation);
            if (max) {
                GlStateManager.color(0.5F, 0.5F, 0.5F, alphaMultiplier * 0.5F);
            } else {
                GlStateManager.color(1F, 1F, 1F, hovered ? 1F : alphaMultiplier * 0.7F);
            }
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
            GlStateManager.disableBlend();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!max) {
            super.playPressSound(soundHandlerIn);
        }
    }

    public boolean isNotMax() {
        return !max;
    }

    public enum ArrowType {
        LEFT("gui/arrowleft.png"),
        RIGHT("gui/arrowright.png");

        final ResourceLocation resourceLocation;

        ArrowType(String path) {
            this.resourceLocation = new ResourceLocation("skyblockaddons", path);
        }
    }
}
