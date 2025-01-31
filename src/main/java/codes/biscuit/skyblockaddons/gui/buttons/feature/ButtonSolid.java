package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class ButtonSolid extends ButtonFeature {

    private static final int DEFAULT_BOX_COLOR = main.getUtils().getDefaultColor(1F);

    private final boolean colorChangeWithFeature;

    private int boxColor;

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid(double x, double y, int width, int height, String buttonText, Feature feature, boolean colorChangeWithFeature) {
        this(x, y, width, height, buttonText, feature, DEFAULT_BOX_COLOR, colorChangeWithFeature);
    }

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid(double x, double y, int width, int height, String buttonText, Feature feature, int boxColor, boolean colorChangeWithFeature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.feature = feature;
        this.width = width;
        this.height = height;
        this.boxColor = boxColor;
        this.colorChangeWithFeature = colorChangeWithFeature;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float alphaMultiplier = calculateAlphaMultiplier();
        int alpha = alphaMultiplier == 1F ? 255 : (int) (255 * alphaMultiplier);
        hovered = isHovered(mouseX, mouseY);
        int boxAlpha = hovered ? 170 : 100;
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        //noinspection lossy-conversions
        boxAlpha *= alphaMultiplier;
        boxColor = ColorUtils.setColorAlpha(boxColor, boxAlpha);
        GlStateManager.enableBlend();
        if (alpha < 4) alpha = 4;
        int fontColor = getFontColor(alpha);
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        float widthLimit = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10;
        float scale = stringWidth > widthLimit ? 1F / (stringWidth / widthLimit) : 1F;
        drawButtonBoxAndText(boxColor, scale, fontColor);
        GlStateManager.disableBlend();
    }

    private int getFontColor(int alpha) {
        int fontColor;
        if (hovered) {
            fontColor = new Color(255, 255, 160, alpha).getRGB();
        } else {
            fontColor = new Color(224, 224, 224, alpha).getRGB();
        }

        if (colorChangeWithFeature) {
            if (hovered) {
                fontColor = feature.isEnabled()
                        ? ColorCode.GREEN.getColor(alpha)
                        : ColorCode.RED.getColor(alpha);
            } else {
                fontColor = feature.isEnabled()
                        ? ColorCode.DARK_GREEN.getColor(alpha)
                        : ColorCode.DARK_RED.getColor(alpha);
            }
        }
        return fontColor;
    }

}
