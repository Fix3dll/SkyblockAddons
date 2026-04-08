package com.fix3dll.skyblockaddons.core.feature;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import lombok.Getter;

@Getter
public class FeatureGuiData {

    private ColorCode defaultColor = null;
    private EnumUtils.DrawType drawType = null;

    public FeatureGuiData(ColorCode defaultColor) {
        this.defaultColor = defaultColor;
    }

    public FeatureGuiData(EnumUtils.DrawType drawType) {
        this.drawType = drawType;
    }

    public FeatureGuiData(EnumUtils.DrawType drawType, ColorCode defaultColor) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
    }

}