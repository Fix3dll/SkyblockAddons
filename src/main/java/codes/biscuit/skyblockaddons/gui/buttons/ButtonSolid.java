package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonText;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonSolid extends ButtonText {

    private final Feature feature;
    /** Used to calculate the transparency when fading in. */
    private final long timeOpened = System.currentTimeMillis();

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid( double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        switch (feature) {
            case TEXT_STYLE:
                displayString = main.getConfigValues().getTextStyle().getMessage();
                break;
            case CHROMA_MODE:
                displayString = main.getConfigValues().getChromaMode().getMessage();
                break;
            case WARNING_TIME:
                displayString = main.getConfigValues().getWarningSeconds()+"s";
                break;
            case SHOW_BACKPACK_PREVIEW:
                displayString = main.getConfigValues().getBackpackStyle().getMessage();
                break;
            case DEPLOYABLE_STATUS_DISPLAY:
                displayString = main.getConfigValues().getDeployableDisplayStyle().getMessage();
                break;
            case TURN_ALL_FEATURES_CHROMA:
                boolean enable = false;
                for (Feature loopFeature : Feature.values()) {
                    if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData().getDefaultColor() != null) {
                        if (!main.getConfigValues().getChromaFeatures().contains(loopFeature)) {
                            enable = true;
                            break;
                        }
                    }
                }
                displayString = Translations.getMessage(enable ? "messages.enableAll" : "messages.disableAll");
                break;
            case PET_DISPLAY:
                displayString = main.getConfigValues().getPetItemStyle().getMessage();
                break;
        }
        float alphaMultiplier = getAlphaMultiplier(timeOpened);
        int alpha = alphaMultiplier == 1F ? 255 : (int) (255 * alphaMultiplier);
        hovered = isHovered(mouseX, mouseY);
        int boxAlpha = 100;
        if (hovered && feature != Feature.WARNING_TIME) boxAlpha = 170;
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        //noinspection lossy-conversions
        boxAlpha *= alphaMultiplier;
        int boxColor = main.getUtils().getDefaultColor(boxAlpha);
        if (this.feature == Feature.RESET_LOCATION) {
            boxColor = ColorUtils.setColorAlpha(0xFF7878, boxAlpha);
        }
        GlStateManager.enableBlend();
        if (alpha < 4) alpha = 4;
        int fontColor = new Color(224, 224, 224, alpha).getRGB();
        if (hovered && feature != Feature.WARNING_TIME) {
            fontColor = new Color(255, 255, 160, alpha).getRGB();
        }
        float scale = 1;
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        float widthLimit = BUTTON_MAX_WIDTH -10;
        if (feature == Feature.WARNING_TIME) {
            widthLimit = 90;
        }
        if (stringWidth > widthLimit) {
            scale = 1/(stringWidth/widthLimit);
        }
        drawButtonBoxAndText(boxColor, boxAlpha, scale, fontColor);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (feature != Feature.WARNING_TIME) super.playPressSound(soundHandlerIn);
    }
}
