package com.fix3dll.skyblockaddons.features.dragontracker;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum DragonType {
    PROTECTOR(ColorCode.DARK_BLUE),
    OLD(ColorCode.GRAY),
    WISE(ColorCode.BLUE),
    UNSTABLE(ColorCode.BLACK),
    YOUNG(ColorCode.WHITE),
    STRONG(ColorCode.RED),
    SUPERIOR(ColorCode.GOLD);

    private final ColorCode color;

    DragonType(ColorCode color) {
        this.color = color;
    }

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." + this.name().toLowerCase(Locale.US));
    }

    public static DragonType fromName(String name) {
        for (DragonType dragonType : DragonType.values()) {
            if (dragonType.name().equals(name.toUpperCase(Locale.US))) {
                return dragonType;
            }
        }

        return null;
    }
}