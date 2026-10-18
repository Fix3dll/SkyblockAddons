package com.fix3dll.skyblockaddons.features.events;

import java.util.Locale;
import java.util.Optional;

public enum JacobCrop {
    WHEAT("Wheat"), CARROT("Carrot"), POTATO("Potato"), PUMPKIN("Pumpkin"), MELON("Melon"),
    SUGAR_CANE("Sugar Cane"), CACTUS("Cactus"), COCOA_BEANS("Cocoa Beans"), MUSHROOM("Mushroom"), NETHER_WART("Nether Wart");

    private final String displayName;

    JacobCrop(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<JacobCrop> fromName(String name) {
        if (name == null) return Optional.empty();
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        normalized = switch (normalized) {
            case "melon slice" -> "melon";
            case "red mushroom", "brown mushroom" -> "mushroom";
            case "seeds" -> "wheat";
            default -> normalized;
        };
        for (JacobCrop crop : values()) {
            if (crop.displayName.toLowerCase(Locale.ROOT).equals(normalized)) return Optional.of(crop);
        }
        return Optional.empty();
    }
}
