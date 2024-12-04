package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonText;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class ButtonModify extends ButtonText {

    private final Feature feature;

    /**
     * Create a button for adding or subtracting a number.
     */
    public ButtonModify(double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = isHovered(mouseX, mouseY);
        int boxColor, fontColor, boxAlpha;
        if (hovered && !hitMaximum()) {
            fontColor = ColorCode.WHITE.getColor();
            boxAlpha = 170;
        } else {
            fontColor = new Color(255, 255, 160, 255).getRGB();
            boxAlpha = 100;
        }
        if (hitMaximum()) {
            boxColor = ColorCode.GRAY.getColor(boxAlpha);
        } else {
            if (feature == Feature.ADD) {
                boxColor = ColorCode.GREEN.getColor(boxAlpha);
            } else {
                boxColor = ColorCode.RED.getColor(boxAlpha);
            }
        }
        GlStateManager.enableBlend();
        drawButtonBoxAndText(boxColor, boxAlpha, 1, fontColor);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!hitMaximum()) {
            super.playPressSound(soundHandlerIn);
        }
    }

    private boolean hitMaximum() {
        ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
        return (feature == Feature.SUBTRACT && config.getWarningSeconds() == 1)
                || (feature == Feature.ADD && config.getWarningSeconds() == 99);
    }
}
