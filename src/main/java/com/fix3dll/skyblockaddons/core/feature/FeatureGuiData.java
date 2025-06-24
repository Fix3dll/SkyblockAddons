package com.fix3dll.skyblockaddons.core.feature;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import lombok.Getter;

@Getter
public class FeatureGuiData {

    private ColorCode defaultColor = null;
    private EnumUtils.DrawType drawType = null;

    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br>
     *
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    private final boolean colorsRestricted;

    public FeatureGuiData(ColorCode defaultColor) {
        this(defaultColor, false);
    }

    public FeatureGuiData(ColorCode defaultColor, boolean colorsRestricted) {
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }

    public FeatureGuiData(EnumUtils.DrawType drawType) {
        this(drawType, false);
    }

    public FeatureGuiData(EnumUtils.DrawType drawType, ColorCode defaultColor) {
        this(drawType, defaultColor, false);
    }

    private FeatureGuiData(EnumUtils.DrawType drawType, boolean colorsRestricted) {
        this.drawType = drawType;
        this.colorsRestricted = colorsRestricted;
    }

    public FeatureGuiData(EnumUtils.DrawType drawType, ColorCode defaultColor, boolean colorsRestricted) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }
}