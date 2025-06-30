package com.fix3dll.skyblockaddons.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains a player's stats. This includes health, mana and defence...
 */
public enum PlayerStat {
    DEFENCE(0),
    HEALTH(100),
    MAX_HEALTH(100),
    MAX_RIFT_HEALTH(0),
    MANA(100),
    MAX_MANA(100),
    FUEL(3000),
    MAX_FUEL(3000),
    OVERFLOW_MANA(20),
    PRESSURE(-1) // -1 is not in water
    ;

    @Getter @Setter private float value;

    PlayerStat(float defaultValue) {
        this.value = defaultValue;
    }

}