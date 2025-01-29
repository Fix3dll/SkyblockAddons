package codes.biscuit.skyblockaddons.features.dungeon;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Getter @ToString
public enum DungeonClass {
    HEALER(Items.potionitem, "Healer", ColorCode.LIGHT_PURPLE),
    MAGE(Items.blaze_rod, "Mage", ColorCode.AQUA),
    BERSERK(Items.iron_sword, "Berserk", ColorCode.DARK_RED),
    ARCHER(Items.bow, "Archer", ColorCode.GOLD),
    TANK(Items.leather_chestplate, "Tank", ColorCode.DARK_GREEN);

    private final char firstLetter;
    private final ItemStack item;
    private final String chatDisplayName; // The way Hypixel writes it out in chat
    private final ColorCode defaultColor;

    DungeonClass(Item item, String chatDisplayName, ColorCode defaultColor) {
        this.firstLetter = this.name().charAt(0);
        this.item = new ItemStack(item);
        this.chatDisplayName = chatDisplayName;
        this.defaultColor = defaultColor;
    }

    public static DungeonClass fromFirstLetter(char firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter == firstLetter) {
                return dungeonClass;
            }
        }
        return null;
    }

    public static DungeonClass fromDisplayName(String name) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.getChatDisplayName().equals(name)) {
                return dungeonClass;
            }
        }
        return null;
    }

    public int getColor() {
        Feature feature = Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY;
        if (feature.isEnabled()) {
            switch (this) {
                case HEALER:
                    return feature.getAsNumber(FeatureSetting.HEALER_COLOR).intValue();
                case MAGE:
                    return feature.getAsNumber(FeatureSetting.MAGE_COLOR).intValue();
                case BERSERK:
                    return feature.getAsNumber(FeatureSetting.BERSERK_COLOR).intValue();
                case ARCHER:
                    return feature.getAsNumber(FeatureSetting.ARCHER_COLOR).intValue();
                case TANK:
                    return feature.getAsNumber(FeatureSetting.TANK_COLOR).intValue();
                default:
                    return this.defaultColor.getColor();
            }
        } else {
            return this.defaultColor.getColor();
        }
    }
}