package com.fix3dll.skyblockaddons.core.render.chroma;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;

import java.awt.Color;

/**
 * This class is used to manual
 */
public class ManualChromaManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
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
        int scale = (int) MC.getWindow().getGuiScale();
        x *= scale;
        y *= scale;

        float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (MC.getWindow().getWidth() / 100F);
        float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;

        float ticks = (float) main.getScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x + y) / chromaSize - timeOffset) % 1;

        //if (currentHSB[2] < 0.3) { // Keep shadows as shadows
        //    return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]), alpha);
        //} else {
        float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();
        float brightness = Feature.CHROMA_BRIGHTNESS.numberValue().floatValue() * currentHSB[2];
        return ARGB.color(alpha, Color.HSBtoRGB(newHue, saturation, brightness));
        //}
    }

    public static int getChromaColor(float x, float y, float z, int alpha) {
        if (Feature.CHROMA_MODE.getValue() == EnumUtils.ChromaMode.ALL_SAME_COLOR) {
            x = 0; y = 0; z = 0;
        }
        float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (MC.getWindow().getWidth() / 100F);
        float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;

        float ticks = (float) main.getScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x - y + z) / (chromaSize / 20F) - timeOffset) % 1;

        float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();
        float brightness = Feature.CHROMA_BRIGHTNESS.numberValue().floatValue();
        return ARGB.color(alpha, Color.HSBtoRGB(newHue, saturation, brightness));
    }

    /**
     * Disables any chroma stuff.
     */
    public static void doneRenderingText() {
        coloringTextChroma = false;
        featureScale = 1;
    }

}