package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
@Getter
public enum InventoryType {
    BASIC_REFORGING("Reforge Item", "Reforge Item"),
    HEX_REFORGING("Hex Reforge", "The Hex ➜ Reforges"),
    SALVAGING("Salvage Items", "Salvage Items"),
    ULTRASEQUENCER("Ultrasequencer", "Ultrasequencer \\((?<type>[a-zA-Z]+)\\)"),
    CHRONOMATRON("Chronomatron", "Chronomatron \\((?<type>[a-zA-Z]+)\\)"),
    SUPERPAIRS("Superpairs", "Superpairs \\((?<type>[a-zA-Z]+)\\)"),
    STORAGE("Storage", "Storage"),
    STORAGE_BACKPACK("BackpackStorage", "(?<type>[a-zA-Z]+) Backpack ?✦? \\(Slot #(?<page>\\d+)\\)"),
    SKILL_TYPE_MENU("Skill Type Menu", "(?<type>[a-zA-Z]+) Skill"),
    ENDER_CHEST("EnderChest", "Ender Chest \\((?<page>\\d+)/\\d+\\)"),
    MAYOR("Mayor", "Mayor (?<mayor>.*)"),
    CALENDAR("Calendar", "Calendar and Events"),
    PETS("Pets","Pets( \\((?<page>\\d+)/\\d+\\) )?"); // "Pets (1/3) "

    private final String inventoryName;
    private final Pattern inventoryPattern;

    InventoryType(String inventoryName, String regex) {
        this.inventoryName = inventoryName;
        this.inventoryPattern = Pattern.compile(regex);
    }
}
