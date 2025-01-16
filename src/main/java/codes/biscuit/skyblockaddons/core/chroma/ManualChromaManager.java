package codes.biscuit.skyblockaddons.core.chroma;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * This class is used to manual
 */
public class ManualChromaManager {

    private static final float[] defaultColorHSB = {0, 0.75F, 0.9F};

    @Getter @Setter private static boolean coloringTextChroma;
    @Getter private static float featureScale;

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br>
     * After calling this & doing the drawString, make sure to call {@link ManualChromaManager#doneRenderingText()}.
     * @param feature The feature to check if fade chroma is enabled.
     */
    public static void renderingText(Feature feature) {
        if (Feature.CHROMA_MODE.getValue() == EnumUtils.ChromaMode.FADE && feature.isChroma()) {
            coloringTextChroma = true;
            featureScale = feature.getGuiScale();
        }
    }

    // TODO Don't force alpha in the future...
    public static int getChromaColor(float x, float y, int alpha) {
        return getChromaColor(x, y, defaultColorHSB, alpha);
    }

    public static int getChromaColor(float x, float y, float[] currentHSB, int alpha) {
        if (Feature.CHROMA_MODE.getValue() == EnumUtils.ChromaMode.ALL_SAME_COLOR) {
            x = 0; y = 0;
        }
        if (coloringTextChroma) {
            x *= featureScale;
            y *= featureScale;
        }
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        x *= scale;
        y *= scale;

        float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (Minecraft.getMinecraft().displayWidth / 100F);
        float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;

        float ticks = (float) SkyblockAddons.getInstance().getScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x + y) / chromaSize - timeOffset) % 1;

        //if (currentHSB[2] < 0.3) { // Keep shadows as shadows
        //    return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]), alpha);
        //} else {
        float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();
        float brightness = Feature.CHROMA_BRIGHTNESS.numberValue().floatValue() * currentHSB[2];
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha);
        //}
    }

    public static int getChromaColor(float x, float y, float z, int alpha) {
        if (Feature.CHROMA_MODE.getValue() == EnumUtils.ChromaMode.ALL_SAME_COLOR) {
            x = 0; y = 0; z = 0;
        }
        float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (Minecraft.getMinecraft().displayWidth / 100F);
        float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;

        float ticks = (float) SkyblockAddons.getInstance().getScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x - y + z) / (chromaSize / 20F) - timeOffset) % 1;

        float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();
        float brightness = Feature.CHROMA_BRIGHTNESS.numberValue().floatValue();
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha);
    }

    /**
     * Disables any chroma stuff.
     */
    public static void doneRenderingText() {
        coloringTextChroma = false;
        featureScale = 1;
    }
}
