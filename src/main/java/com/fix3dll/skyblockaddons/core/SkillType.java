package com.fix3dll.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public enum SkillType {
    FARMING("Farming", Items.GOLDEN_HOE, false),
    MINING("Mining", Items.DIAMOND_PICKAXE, false),
    COMBAT("Combat", Items.IRON_SWORD, false),
    FORAGING("Foraging", Blocks.OAK_SAPLING.asItem(), false),
    FISHING("Fishing", Items.FISHING_ROD, false),
    ENCHANTING("Enchanting", Blocks.ENCHANTING_TABLE.asItem(), false),
    ALCHEMY("Alchemy", Items.BREWING_STAND, false),
    CARPENTRY("Carpentry", Items.CRAFTING_TABLE.asItem(), false),
    RUNECRAFTING("Runecrafting", Items.MAGMA_CREAM, true),
    TAMING("Taming", Items.POLAR_BEAR_SPAWN_EGG, false),
    DUNGEONEERING("Dungeoneering", Blocks.DEAD_BUSH.asItem(), false),
    SOCIAL("Social", Items.CAKE, true),
    HUNTING("Hunting", Items.LEAD, false);

    private final String skillName;
    @Getter private final ItemStack item;
    @Getter private final boolean cosmetic;

    SkillType(String skillName, Item item, boolean isCosmetic) {
        this.skillName = skillName;
        this.item = new ItemStack(item);
        this.cosmetic = isCosmetic;
    }

    public static SkillType getFromString(String text) {
        for (SkillType skillType : values()) {
            if (skillType.skillName != null && skillType.skillName.equals(text)) {
                return skillType;
            }
        }
        return null;
    }
}
