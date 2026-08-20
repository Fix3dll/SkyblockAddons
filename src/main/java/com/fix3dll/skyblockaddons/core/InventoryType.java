package com.fix3dll.skyblockaddons.core;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
public enum InventoryType {
    BASIC_REFORGING("Reforge Item"),
    HEX_REFORGING("Hex Reforge"),
    SALVAGING("Salvage Items"),
    ULTRASEQUENCER("Ultrasequencer"),
    CHRONOMATRON("Chronomatron"),
    SUPERPAIRS("Superpairs"),
    EXP_TABLE_RNG("Experimentation Table RNG"),
    STORAGE("Storage"),
    STORAGE_BACKPACK("BackpackStorage"),
    SKILL_TYPE_MENU("Skill Type Menu"),
    ENDER_CHEST("EnderChest"),
    MAYOR("Mayor"),
    CALENDAR("Calendar"),
    PETS("Pets"),
    EQUIPMENT("Your Equipment and Stats"),
    SKYBLOCK_MENU("SkyBlock Menu"),
    CATACOMBS_CHEST("Catacombs Chest"),
    KUUDRA_CHEST("Kuudra Chest"),
    CROSEUS_CHEST_MENU("Croseus Chest Menu"),
    EQUIPMENT_SETS("Equipment Sets"),
    LOADOUTS("Loadouts"),
    FISHING_BAG("Fishing Bag");

    @Getter private final String inventoryName;

    /** The compiled pattern loaded from regex.json */
    @Setter private volatile Pattern inventoryPattern;

    InventoryType(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    /**
     * Returns the compiled pattern for this inventory.
     * @return the compiled Pattern
     * @throws IllegalStateException if the pattern is not yet loaded
     */
    public Pattern getPattern() {
        Pattern p = this.inventoryPattern;
        if (p == null) {
            throw new IllegalStateException("Inventory pattern has not been initialized: " + this.name());
        }
        return p;
    }

    /**
     * Creates a Matcher for the given title.
     * @param title the inventory title
     * @return a Matcher object
     */
    public Matcher matcher(String title) {
        return getPattern().matcher(title);
    }
}