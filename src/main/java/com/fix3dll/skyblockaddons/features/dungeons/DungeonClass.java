package com.fix3dll.skyblockaddons.features.dungeons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter @ToString
public enum DungeonClass {
    HEALER(Items.SPLASH_POTION.getDefaultInstance(), "Healer", ColorCode.LIGHT_PURPLE),
    ARCHER(Items.BOW.getDefaultInstance(), "Archer", ColorCode.GOLD),
    TANK(Items.LEATHER_CHESTPLATE.getDefaultInstance(), "Tank", ColorCode.DARK_GREEN),
    MAGE(Items.BLAZE_ROD.getDefaultInstance(), "Mage", ColorCode.AQUA),
    BERSERK(Items.IRON_SWORD.getDefaultInstance(), "Berserk", ColorCode.DARK_RED);

    private final char firstLetter;
    private final ItemStack item;
    private final String chatDisplayName; // The way Hypixel writes it out in chat
    private final ColorCode defaultColor;

    DungeonClass(ItemStack item, String chatDisplayName, ColorCode defaultColor) {
        this.firstLetter = this.name().charAt(0);
        this.item = item;
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
            return switch (this) {
                case HEALER -> feature.getAsNumber(FeatureSetting.HEALER_COLOR).intValue();
                case MAGE -> feature.getAsNumber(FeatureSetting.MAGE_COLOR).intValue();
                case BERSERK -> feature.getAsNumber(FeatureSetting.BERSERK_COLOR).intValue();
                case ARCHER -> feature.getAsNumber(FeatureSetting.ARCHER_COLOR).intValue();
                case TANK -> feature.getAsNumber(FeatureSetting.TANK_COLOR).intValue();
                default -> this.defaultColor.getColor();
            };
        } else {
            return this.defaultColor.getColor();
        }
    }
}
