package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.util.LinkedList;

// TODO merge with ColorCode
@Accessors(chain = true)
public class SkyblockColor {

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    @Getter @Setter private ColorAnimation colorAnimation = ColorAnimation.NONE;

    private final LinkedList<Integer> colors = new LinkedList<>();

    public SkyblockColor() {
        this(DEFAULT_COLOR);
    }

    public SkyblockColor(int color) {
        this.colors.add(color);
    }

    public SkyblockColor(int color, float alpha) {
        this.colors.add(ColorUtils.setColorAlpha(color, alpha));
    }

    public SkyblockColor(int r, int g, int b, int a) {
        this.colors.add(ColorUtils.getColor(r, g, b, a));
    }

    public SkyblockColor(int r, int g, int b, float a) {
        this.colors.add(ColorUtils.getColor(r, g, b, ColorUtils.getAlphaIntFromFloat(a)));
    }

    public int getColorAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, ColorUtils.getAlpha(getColor()));
        }

        return colors.get(0);
    }

    public int getTintAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            int color = getColor();
            return ManualChromaManager.getChromaColor(
                    x, y,
                    Color.RGBtoHSB(
                            ColorUtils.getRed(color),
                            ColorUtils.getGreen(color),
                            ColorUtils.getBlue(color),
                            null
                    ),
                    ColorUtils.getAlpha(color)
            );
        }

        return colors.get(0);
    }

    public int getColorAtPosition(double x, double y, double z) {
        return getColorAtPosition((float) x, (float) y, (float) z);
    }

    public int getColorAtPosition(float x, float y, float z) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, z, ColorUtils.getAlpha(getColor()));
        }

        return colors.get(0);
    }

    public SkyblockColor setColor(int color) {
        return setColor(0, color);
    }

    public SkyblockColor setColor(int index, int color) {
        if (index >= colors.size()) {
            colors.add(color);
        } else {
            colors.set(index, color);
        }
        return this;
    }

    public boolean isPositionalMulticolor() {
        return colorAnimation != ColorAnimation.NONE && Feature.CHROMA_MODE.getValue() != EnumUtils.ChromaMode.ALL_SAME_COLOR;
    }

    public int getColor() {
        return getColorSafe(0);
    }

    private int getColorSafe(int index) {
        while (index >= colors.size()) {
            colors.add(DEFAULT_COLOR);
        }
        return colors.get(index);
    }

    public boolean drawMulticolorManually() {
        return colorAnimation == ColorAnimation.CHROMA && !shouldUseChromaShaders();
    }

    public boolean drawMulticolorUsingShader() {
        return colorAnimation == ColorAnimation.CHROMA && shouldUseChromaShaders();
    }

    public static boolean shouldUseChromaShaders() {
        return Feature.CHROMA_MODE.getValue() != EnumUtils.ChromaMode.ALL_SAME_COLOR && ShaderManager.getInstance().areShadersSupported();
    }

    public enum ColorAnimation {
        NONE,
        CHROMA
    }
}
