package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

public class ButtonSolid extends ButtonFeature {

    public static final int DEFAULT_BOX_COLOR = main.getUtils().getDefaultColor(1F);

    private final boolean colorChangeWithFeature;
    private final String descriptionTranslationKey;

    private int boxColor;
    private Runnable onClick;

    public ButtonSolid(double x, double y, int width, int height, String buttonText, Runnable onClick) {
        this(x, y, width, height, DEFAULT_BOX_COLOR, buttonText, onClick);
    }

    public ButtonSolid(double x, double y, int width, int height, int boxColor, String buttonText, Runnable onClick) {
        this(x, y, width, height, boxColor, buttonText, onClick, null);
    }

    public ButtonSolid(double x, double y, int width, int height, int boxColor, String buttonText, Runnable onClick, String descriptionTranslationKey) {
        super((int) x, (int) y, Component.literal(buttonText), null);
        this.width = width;
        this.height = height;
        this.boxColor = boxColor;
        this.onClick = onClick;
        this.colorChangeWithFeature = false;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

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
        this(x, y, width, height, buttonText, feature, boxColor, colorChangeWithFeature, null);
    }

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid(double x, double y, int width, int height, String buttonText, Feature feature, int boxColor, boolean colorChangeWithFeature, String descriptionTranslationKey) {
        super((int) x, (int) y, Component.literal(buttonText), feature);
        this.width = width;
        this.height = height;
        this.boxColor = boxColor;
        this.colorChangeWithFeature = colorChangeWithFeature;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float alphaMultiplier = calculateAlphaMultiplier();
        int alpha = alphaMultiplier == 1F ? 255 : (int) (255 * alphaMultiplier);
        this.isHovered = isHovered(mouseX, mouseY);
        if (isHovered && descriptionTranslationKey != null) {
            graphics.setTooltipForNextFrame(MC.font, Component.literal(Translations.getMessage(descriptionTranslationKey)), mouseX, mouseY);
        }
        int boxAlpha = this.isHovered ? 170 : 100;
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        //noinspection lossy-conversions
        boxAlpha *= alphaMultiplier;
        boxColor = ARGB.color(boxAlpha, boxColor);
        if (alpha < 4) alpha = 4;
        int fontColor = getFontColor(alpha);
        int stringWidth = MC.font.width(getMessage());
        float widthLimit = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10;
        this.scale = stringWidth > widthLimit ? 1F / (stringWidth / widthLimit) : 1F;
        drawButtonBoxAndText(graphics, boxColor, scale, fontColor);
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (onClick != null) onClick.run();
        super.onClick(event, doubleClick);
    }

    @Override
    public void playDownSound(@NonNull SoundManager soundManager) {
        if (feature != Feature.WARNING_TIME) playButtonClickSound(soundManager);
    }

    private int getFontColor(int alpha) {
        int fontColor;
        if (this.isHovered) {
            fontColor = ARGB.color(alpha, 255, 255, 160);
        } else {
            fontColor = ARGB.color(alpha, 224, 224, 224);
        }

        if (colorChangeWithFeature) {
            if (this.isHovered) {
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