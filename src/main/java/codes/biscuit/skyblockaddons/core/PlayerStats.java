package codes.biscuit.skyblockaddons.core;

import org.apache.commons.lang3.mutable.MutableFloat;

/**
 * Contains a player's stats. This includes health, mana and defence...
 */
public enum PlayerStats {

    DEFENCE(new MutableFloat(0)),
    HEALTH(new MutableFloat(100)),
    MAX_HEALTH(new MutableFloat(100)),
    MAX_RIFT_HEALTH(new MutableFloat(0)),
    MANA(new MutableFloat(100)),
    MAX_MANA(new MutableFloat(100)),
    FUEL(new MutableFloat(3000)),
    MAX_FUEL(new MutableFloat(3000)),
    OVERFLOW_MANA(new MutableFloat(20));

    private final MutableFloat mutableValue;

    PlayerStats(MutableFloat defaultValue) {
        this.mutableValue = defaultValue;
    }

    public float getValue() {
        return mutableValue.getValue();
    }

    public void setValue(final float value) {
        mutableValue.setValue(value);
    }
}
