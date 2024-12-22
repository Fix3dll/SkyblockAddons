package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;

/**
 * Skyblock item rarity definitions
 * @see <a href="https://wiki.hypixel.net/Rarity">https://wiki.hypixel.net/Rarity</a>
 */
@Getter
public enum SkyblockRarity {

    COMMON("COMMON", ColorCode.WHITE),
    UNCOMMON("UNCOMMON", ColorCode.GREEN),
    RARE("RARE", ColorCode.BLUE),
    EPIC("EPIC", ColorCode.DARK_PURPLE),
    LEGENDARY("LEGENDARY", ColorCode.GOLD),
    MYTHIC("MYTHIC", ColorCode.LIGHT_PURPLE),
    DIVINE("DIVINE", ColorCode.AQUA),
    SPECIAL("SPECIAL", ColorCode.RED),
    VERY_SPECIAL("VERY SPECIAL", ColorCode.RED),
    ULTIMATE("ULTIMATE", ColorCode.DARK_RED),
    ADMIN("ADMIN", ColorCode.DARK_RED);

    /** The name of the rarity as displayed in an item's lore */
    private final String loreName;
    /** The color code for the color of the rarity as it's displayed in an item's lore */
    private final ColorCode colorCode;

    SkyblockRarity(String loreName, ColorCode colorCode) {
        this.loreName = loreName;
        this.colorCode = colorCode;
    }

    public static SkyblockRarity getByLoreName(String loreName) {
        for (SkyblockRarity rarity : SkyblockRarity.values()) {
            if (rarity.getLoreName().equalsIgnoreCase(loreName)) {
                return rarity;
            }
        }
        return null;
    }

    // FIXME SPECIAL and VERY_SPECIAL same color
    public static SkyblockRarity getByColorCode(ColorCode colorCode) {
        for (SkyblockRarity rarity : SkyblockRarity.values()) {
            if (rarity.getColorCode().equals(colorCode)) {
                return rarity;
            }
        }
        return null;
    }
}
