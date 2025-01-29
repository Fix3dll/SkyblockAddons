package codes.biscuit.skyblockaddons.core;

import org.apache.commons.lang3.mutable.MutableFloat;

/**
 * Contains a player's stats. This includes health, mana and defence...
 */
public enum PlayerStats {
    DEFENCE(0),
    HEALTH(100),
    MAX_HEALTH(100),
    MAX_RIFT_HEALTH(0),
    MANA(100),
    MAX_MANA(100),
    FUEL(3000),
    MAX_FUEL(3000),
    OVERFLOW_MANA(20);

    private final MutableFloat mutableValue;

    PlayerStats(float defaultValue) {
        this.mutableValue = new MutableFloat(defaultValue);
    }

    public float getValue() {
        return this.mutableValue.getValue();
    }

    public void setValue(final float value) {
        this.mutableValue.setValue(value);
    }
}
