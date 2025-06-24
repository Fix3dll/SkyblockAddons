package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.util.ARGB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class ColorUtils {

    private static final SkyblockColor SKYBLOCK_COLOR = new SkyblockColor();

    public static boolean areAllFeaturesChroma() {
        for (Feature loopFeature : Feature.values()) {
            if (loopFeature.isGuiFeature() && loopFeature.getFeatureGuiData().getDefaultColor() != null) {
                if (!loopFeature.isChroma()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int getDefaultBlue(int alpha) {
        return ARGB.color(alpha, 160, 225, 229);
    }

    /**
     * Takes the color input integer and sets its alpha color value,
     * returning the resulting color.
     */
    public static int setColorAlpha(int color, float alpha) {
        return ARGB.color(getAlphaIntFromFloat(alpha),color );
    }

    /**
     * @see <a href="https://github.com/hannibal002/SkyHanni/pull/1660">ThatGravyBoat SkyHanni PR</a>
     */
    public static float getAlpha() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            buffer.clear();
            GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, buffer);

            if (buffer.limit() < 4) return 1f;
            return buffer.get(3);
        }
    }

    public static int getAlphaIntFromFloat(float alpha) {
        return (int) (alpha * 255);
    }

    public static float[] getNormalizedRGBA(int color) {
        return new float[] {
                (float) (color >> 16 & 255) / 255.0F,
                (float) (color >> 8 & 255) / 255.0F,
                (float) (color & 255) / 255.0F,
                (float) (color >> 24 & 255) / 255.0F,
        };
    }

    public static SkyblockColor getDummySkyblockColor(int color) {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, color);
    }

    public static SkyblockColor getDummySkyblockColor(int r, int g, int b, int a) {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, ARGB.color(a, r, g, b));
    }

    public static SkyblockColor getDummySkyblockColor(int r, int g, int b, float a) {
        return getDummySkyblockColor(r, g, b, getAlphaIntFromFloat(a));
    }

    public static SkyblockColor getDummySkyblockColor(SkyblockColor.ColorAnimation colorAnimation) {
        return getDummySkyblockColor(colorAnimation, -1);
    }

    public static SkyblockColor getDummySkyblockColor(int color, boolean chroma) {
        return getDummySkyblockColor(chroma ? SkyblockColor.ColorAnimation.CHROMA : SkyblockColor.ColorAnimation.NONE, color);
    }

    public static SkyblockColor getDummySkyblockColor(SkyblockColor.ColorAnimation colorAnimation, int color) {
        return SKYBLOCK_COLOR.setColorAnimation(colorAnimation).setColor(color);
    }
}
