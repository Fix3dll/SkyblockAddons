package com.fix3dll.skyblockaddons.core;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum CrimsonArmorAbilityStack {
    CRIMSON("Crimson", "Dominus", "ᝐ"),
    TERROR("Terror", "Hydra Strike", "⁑"),
    AURORA("Aurora", "Arcane Energy", "Ѫ"),
    FERVOR("Fervor", "Fervor", "҉"),
    HOLLOW("Hollow", "Spirit", "⚶");

    private final String armorName;
    private final String abilityName;
    private final String symbol;

    @SuppressWarnings("NonFinalFieldInEnum") //lombok plugin moment
    @Setter private int currentValue = 0;

    CrimsonArmorAbilityStack(String armorName, String abilityName, String symbol) {
        this.armorName = armorName;
        this.abilityName = abilityName;
        this.symbol = symbol;
    }

}