package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.core.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.ARGB;

import java.awt.*;
import java.util.LinkedList;

@Accessors(chain = true)
public class SkyblockColor {

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    @Getter
    @Setter
    private ColorAnimation colorAnimation = ColorAnimation.NONE;

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
        this.colors.add(ARGB.color(a, r, g, b));
    }

    public SkyblockColor(int r, int g, int b, float a) {
        this.colors.add(ARGB.color(ColorUtils.getAlphaIntFromFloat(a), r, g, b));
    }

    public int getColorAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, ARGB.alpha(getColor()));
        }

        return colors.getFirst();
    }

    public int getTintAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, Color.RGBtoHSB(ARGB.red(getColor()), ARGB.green(getColor()), ARGB.blue(getColor()), null) , ARGB.alpha(getColor()));
        }

        return colors.getFirst();
    }

    public int getColorAtPosition(double x, double y, double z) {
        return getColorAtPosition((float) x, (float) y, (float) z);
    }

    public int getColorAtPosition(float x, float y, float z) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, z, ARGB.alpha(getColor()));
        }

        return colors.getFirst();
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

    public boolean isMulticolor() {
        return colorAnimation != ColorAnimation.NONE;
    }

//    public boolean isPositionalMulticolor() {
//        return colorAnimation != ColorAnimation.NONE && SkyblockAddons.getInstance().getConfigValues().getChromaMode() != EnumUtils.ChromaMode.ALL_SAME_COLOR;
//    }

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
        return Feature.CHROMA_MODE.getValue() != EnumUtils.ChromaMode.ALL_SAME_COLOR;
    }

    public enum ColorAnimation {
        NONE,
        CHROMA
    }
}
