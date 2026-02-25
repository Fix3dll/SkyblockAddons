package com.fix3dll.skyblockaddons.features.slayertracker;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumMap;

@Getter
public enum SlayerDrop {

    // Revenant Drops
    REVENANT_FLESH(Items.ROTTEN_FLESH, "Revenant Flesh", "REVENANT_FLESH", SkyblockRarity.UNCOMMON, true),
    FOUL_FLESH(Items.COAL, "Foul Flesh", "FOUL_FLESH", SkyblockRarity.RARE),
    REVENANT_SHARD("REVENANT_SHARD", "undead_essence", "R27", SkyblockRarity.RARE),
    PESTILENCE_RUNE("PESTILENCE_RUNE", "ZOMBIE_SLAYER", SkyblockRarity.RARE),
    UNDEAD_CATALYST("UNDEAD_CATALYST", SkyblockRarity.RARE),
    SMITE_SIX(SkyblockRarity.RARE, "smite", 6),
    BEHEADED_HORROR("BEHEADED_HORROR", SkyblockRarity.EPIC),
    REVENANT_CATALYST("REVENANT_CATALYST", SkyblockRarity.EPIC),
    SNAKE_RUNE("SNAKE_RUNE", "SNAKE", SkyblockRarity.LEGENDARY),
    FESTERING_MAGGOT("FESTERING_MAGGOT", SkyblockRarity.EPIC),
    REVENANT_VISCERA(Items.COOKED_PORKCHOP, "Revenant Viscera", "REVENANT_VISCERA", SkyblockRarity.RARE, true),
    SCYTHE_BLADE(Items.DIAMOND, "Scythe Blade", "SCYTHE_BLADE", SkyblockRarity.LEGENDARY, true),
    SMITE_SEVEN(SkyblockRarity.EPIC, "smite", 7),
    SEVERED_HAND("SEVERED_HAND", SkyblockRarity.LEGENDARY),
    SHARD_OF_SHREDDED("SHARD_OF_THE_SHREDDED", SkyblockRarity.LEGENDARY),
    WARDEN_HEART("WARDEN_HEART", SkyblockRarity.LEGENDARY),
    DYE_MATCHA("DYE_MATCHA", SkyblockRarity.LEGENDARY),

    // Tarantula Drops
    TARANTULA_WEB(Items.STRING, "Tarantula Web", "TARANTULA_WEB", SkyblockRarity.UNCOMMON, true),
    TOXIC_ARROW_POISON(Items.LIME_DYE, "Toxic Arrow Poison", "TOXIC_ARROW_POISON", SkyblockRarity.RARE),
    BITE_RUNE("BITE_RUNE", "BITE", SkyblockRarity.EPIC),
    DARKNESS_WITHIN_RUNE("DARKNESS_WITHIN_RUNE", "DARKNESS_WITHIN", SkyblockRarity.EPIC),
    SPIDER_CATALYST("SPIDER_CATALYST", SkyblockRarity.RARE),
    TARANTULA_SILK(Items.COBWEB, "Tarantula Silk", "TARANTULA_SILK",  SkyblockRarity.RARE, true),
    BANE_OF_ARTHROPODS_SIX(SkyblockRarity.RARE, "bane_of_arthropods", 6),
    TARANTULA_CATALYST("TARANTULA_CATALYST", SkyblockRarity.EPIC),
    FLY_SWATTER(Items.GOLDEN_SHOVEL, "Fly Swatter", "FLY_SWATTER", SkyblockRarity.EPIC, true),
    VIAL_OF_VENOM("VIAL_OF_VENOM", SkyblockRarity.EPIC),
    TARANTULA_TALISMAN("TARANTULA_TALISMAN", SkyblockRarity.EPIC),
    DIGESTED_MOSQUITO(Items.ROTTEN_FLESH, "Digested Mosquito", "DIGESTED_MOSQUITO", SkyblockRarity.LEGENDARY),
    SHRIVELED_WASP("SHRIVELED_WASP", SkyblockRarity.LEGENDARY),
    ENSNARED_SNAIL("ENSNARED_SNAIL", SkyblockRarity.LEGENDARY),
    PRIMORDIAL_EYE("PRIMORDIAL_EYE", SkyblockRarity.LEGENDARY),
    DYE_BRICK_RED("DYE_BRICK_RED", SkyblockRarity.LEGENDARY),

    // Sven Drops
    WOLF_TOOTH(Items.GHAST_TEAR, "Wolf Tooth", "WOLF_TOOTH", SkyblockRarity.UNCOMMON, true),
    HAMSTER_WHEEL(Blocks.OAK_TRAPDOOR.asItem(), "Hamster Wheel", "HAMSTER_WHEEL", SkyblockRarity.RARE, true),
    SPIRIT_RUNE("SPIRIT_RUNE", "SPIRIT", SkyblockRarity.RARE),
    CRITICAL_SIX(SkyblockRarity.RARE, "critical", 6),
    FURBALL("FURBALL", SkyblockRarity.RARE),
    RED_CLAW_EGG(Items.MOOSHROOM_SPAWN_EGG, "Red Claw Egg", "RED_CLAW_EGG", SkyblockRarity.EPIC),
    COUTURE_RUNE("COUTURE_RUNE", "COUTURE", SkyblockRarity.LEGENDARY),
    OVERFLUX_CAPACITOR(Items.QUARTZ, "Overflux Capacitor", "OVERFLUX_CAPACITOR", SkyblockRarity.EPIC),
    GRIZZLY_BAIT(Items.SALMON, "Grizzly Salmon", "GRIZZLY_BAIT", SkyblockRarity.RARE),
    DYE_CELESTE("DYE_CELESTE",  SkyblockRarity.LEGENDARY),

    // Enderman Drops
    NULL_SPHERE(Items.FIREWORK_STAR, "Null Sphere", "NULL_SPHERE", SkyblockRarity.UNCOMMON, true),
    TWILIGHT_ARROW_POISON(Items.PURPLE_DYE, "Twilight Arrow Poison", "TWILIGHT_ARROW_POISON", SkyblockRarity.UNCOMMON),
    ENDERSNAKE_RUNE("ENDERSNAKE_RUNE", "ENDERSNAKE", SkyblockRarity.LEGENDARY),
    SUMMONING_EYE("SUMMONING_EYE", SkyblockRarity.EPIC),
    MANA_STEAL_ONE(SkyblockRarity.RARE, "mana_steal", 1),
    TRANSMISSION_TUNER("TRANSMISSION_TUNER", SkyblockRarity.EPIC),
    NULL_ATOM(Blocks.OAK_BUTTON.asItem(), "Null Atom", "NULL_ATOM", SkyblockRarity.RARE, true),
    HAZMAT_ENDERMAN("HAZMAT_ENDERMAN", SkyblockRarity.LEGENDARY),
    POCKET_ESPRESSO_MACHINE("POCKET_ESPRESSO_MACHINE", SkyblockRarity.COMMON),
    SMARTY_PANTS_ONE(SkyblockRarity.RARE, "smarty_pants", 1),
    END_RUNE("END_RUNE", "ENDERSNAKE", SkyblockRarity.EPIC),
    HANDY_BLOOD_CHALICE("HANDY_BLOOD_CHALICE", SkyblockRarity.COMMON),
    SINFUL_DICE("SINFUL_DICE", SkyblockRarity.EPIC),
    EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER("EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER", SkyblockRarity.LEGENDARY),
    VOID_CONQUEROR_ENDERMAN_SKIN("PET_SKIN_ENDERMAN_SLAYER", SkyblockRarity.EPIC),
    ETHERWARP_MERGER("ETHERWARP_MERGER", SkyblockRarity.EPIC),
    JUDGEMENT_CORE("JUDGEMENT_CORE", SkyblockRarity.LEGENDARY),
    ENCHANT_RUNE("ENCHANT_RUNE", "ENCHANT", SkyblockRarity.LEGENDARY),
    ENDER_SLAYER_SEVEN(SkyblockRarity.RARE, "ender_slayer", 7),
    ENDSTONE_IDOL("ENDSTONE_IDOL", SkyblockRarity.LEGENDARY),
    DYE_BYZANTIUM("DYE_BYZANTIUM", SkyblockRarity.LEGENDARY),

    // Blaze Drops
    DERELICT_ASHE(Items.GUNPOWDER, "Derelict Ashe", "DERELICT_ASHE", SkyblockRarity.UNCOMMON, true),
    ENCHANTED_BLAZE_POWDER(Items.BLAZE_POWDER, "Blaze Powder", "ENCHANTED_BLAZE_POWDER", SkyblockRarity.UNCOMMON, true),
    LAVATEARS_RUNE("LAVATEARS_RUNE", "LAVATEARS", SkyblockRarity.LEGENDARY),
    WISPS_ICE_FLAVORED_WATER(Items.SPLASH_POTION, "Wisp's Ice-Flavored Water I Splash Potion", "POTION", SkyblockRarity.COMMON),
    BUNDLE_OF_MAGMA_ARROWS("ARROW_BUNDLE_MAGMA", SkyblockRarity.EPIC),
    MANA_DISINTEGRATOR("MANA_DISINTEGRATOR", SkyblockRarity.RARE),
    SCORCHED_BOOKS("SCORCHED_BOOKS", SkyblockRarity.MYTHIC),
    KELVIN_INVERTER("KELVIN_INVERTER", SkyblockRarity.RARE),
    BLAZE_ROD_DISTILLATE("BLAZE_ROD_DISTILLATE", SkyblockRarity.RARE),
    GLOWSTONE_DISTILLATE("GLOWSTONE_DUST_DISTILLATE", SkyblockRarity.RARE),
    MAGMA_CREAM_DISTILLATE("MAGMA_CREAM_DISTILLATE", SkyblockRarity.RARE),
    NETHER_WART_DISTILLATE("NETHER_STALK_DISTILLATE", SkyblockRarity.RARE),
    GABAGOOL_DISTILLATE("CRUDE_GABAGOOL_DISTILLATE", SkyblockRarity.RARE),
    SCORCHED_POWER_CRYSTAL("SCORCHED_POWER_CRYSTAL", SkyblockRarity.LEGENDARY),
    ARCHFIEND_DICE("ARCHFIEND_DICE", SkyblockRarity.EPIC),
    FIRE_ASPECT_THREE(SkyblockRarity.COMMON, "fire_aspect", 3),
    FIERY_BURST_RUNE("FIERY_BURST_RUNE", "FIERY_BURST", SkyblockRarity.LEGENDARY),
    FLAWED_OPAL_GEMSTONE("FLAWED_OPAL_GEM", SkyblockRarity.UNCOMMON),
    DUPLEX(SkyblockRarity.COMMON, "ultimate_reiterate", 1),
    HIGH_CLASS_ARCHFIEND_DICE("HIGH_CLASS_ARCHFIEND_DICE", SkyblockRarity.LEGENDARY),
    WILSON_ENGINEERING_PLANS(Items.PAPER, "Wilson's Engineering Plans", "WILSON_ENGINEERING_PLANS", SkyblockRarity.LEGENDARY, true),
    SUBZERO_INVERTER("SUBZERO_INVERTER", SkyblockRarity.LEGENDARY),
    DYE_FLAME("DYE_FLAME",  SkyblockRarity.LEGENDARY),

    //Vampire Slayer
    COVEN_SEAL(Items.NETHER_WART, "Coven Seal", "COVEN_SEAL", SkyblockRarity.UNCOMMON),
    QUANTUM_BOOK_BUNDLE("ENCHANTED_BOOK_BUNDLE_QUANTUM", SkyblockRarity.UNCOMMON),
    SOULTWIST_RUNE("SOULTWIST_RUNE", "SOULTWIST", SkyblockRarity.EPIC),
    BUBBA_BLISTER("BUBBA_BLISTER", SkyblockRarity.LEGENDARY),
    FANGTASTIC_CHOCOLATE_CHIP(Items.COOKIE, "Fang-tastic Chocolate Chip", "CHOCOLATE_CHIP", SkyblockRarity.LEGENDARY),
    GUARDIAN_LUCKY_BLOCK("GUARDIAN_LUCKY_BLOCK", SkyblockRarity.LEGENDARY),
    MCGRUBBERS_BURGER("MCGRUBBER_BURGER", SkyblockRarity.EPIC),
    UNFANGED_VAMPIRE_PART("UNFANGED_VAMPIRE_PART", SkyblockRarity.LEGENDARY),
    THE_ONE_BOOK_BUNDLE("ENCHANTED_BOOK_BUNDLE_THE_ONE", SkyblockRarity.LEGENDARY),
    DYE_SANGRIA("DYE_SANGRIA", SkyblockRarity.LEGENDARY);

    private final String skyblockID;
    private final SkyblockRarity rarity;
    private final ItemStack itemStack;
    private String runeID;
    private String attributeNbtKey;
    private String attributeID;

    /**
     * Creates an enchanted book slayer drop with rarity, enchant nbt name and enchant level.
     */
    SlayerDrop(SkyblockRarity rarity, String enchantID, int enchantLevel) {
        this.itemStack = ItemUtils.createEnchantedBook(rarity, enchantID, enchantLevel);
        this.skyblockID = "ENCHANTED_BOOK";
        this.rarity = rarity;
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id and item rarity
     */
    SlayerDrop(Item item, String name, String skyblockID, SkyblockRarity rarity) {
        this(item, name, skyblockID, rarity, false);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, item rarity and enchanted state
     */
    SlayerDrop(Item item, String name, String skyblockID, SkyblockRarity rarity, boolean enchanted) {
        this.itemStack = ItemUtils.createItemStack(item, name, skyblockID, enchanted);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a slayer drop with textured skull from {@link ItemUtils#getTexturedHead(String)} with skyblockId
     */
    SlayerDrop(String skyblockID, SkyblockRarity rarity) {
        this.itemStack = ItemUtils.getTexturedHead(skyblockID);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a rune slayer drop with identifier and runeId field
     */
    SlayerDrop(String identifier, String runeID, SkyblockRarity rarity) {
        this.itemStack = ItemUtils.getTexturedHead(identifier);
        this.skyblockID = "RUNE";
        this.rarity = rarity;
        this.runeID = runeID;
    }

    /**
     * Creates an attribute shard slayer drop with identifier, attributeNbtKey and attributeID field
     */
    SlayerDrop(String identifier, String attributeNbtKey, String attributeID, SkyblockRarity rarity) {
        this.itemStack = ItemUtils.getTexturedHead(identifier);
        this.skyblockID = "ATTRIBUTE_SHARD";
        this.rarity = rarity;
        this.attributeNbtKey = attributeNbtKey;
        this.attributeID = attributeID;
    }

    private static final EnumMap<SlayerDrop, Component> internalItemTranslations = new EnumMap<>(SlayerDrop.class);

    static {
        // revenant
        internalItemTranslations.put(REVENANT_FLESH, Component.literal("Revenant Flesh"));
        internalItemTranslations.put(FOUL_FLESH, Component.literal("Foul Flesh"));
        internalItemTranslations.put(REVENANT_SHARD, Component.literal("Revenant"));
        internalItemTranslations.put(PESTILENCE_RUNE, Component.literal("◆ Pestilence Rune"));
        internalItemTranslations.put(UNDEAD_CATALYST, Component.literal("Undead Catalyst"));
        internalItemTranslations.put(SMITE_SIX, Component.literal("Smite 6"));
        internalItemTranslations.put(BEHEADED_HORROR, Component.literal("Beheaded Horror"));
        internalItemTranslations.put(REVENANT_CATALYST, Component.literal("Revenant Catalyst"));
        internalItemTranslations.put(SNAKE_RUNE, Component.literal("◆ Snake Rune"));
        internalItemTranslations.put(FESTERING_MAGGOT, Component.literal("Festering Maggot"));
        internalItemTranslations.put(REVENANT_VISCERA, Component.literal("Revenant Viscera"));
        internalItemTranslations.put(SCYTHE_BLADE, Component.literal("Scythe Blade"));
        internalItemTranslations.put(SMITE_SEVEN, Component.literal("Smite 7"));
        internalItemTranslations.put(SHARD_OF_SHREDDED, Component.literal("Shard of Shredded"));
        internalItemTranslations.put(WARDEN_HEART, Component.literal("Warden Heart"));
        internalItemTranslations.put(DYE_MATCHA, Component.literal("Matcha Dye"));

        // tarantula
        internalItemTranslations.put(TARANTULA_WEB, Component.literal("Tarantula Web"));
        internalItemTranslations.put(TOXIC_ARROW_POISON, Component.literal("Toxic Arrow Poison"));
        internalItemTranslations.put(BITE_RUNE, Component.literal("◆ Bite Rune"));
        internalItemTranslations.put(DARKNESS_WITHIN_RUNE, Component.literal("◆ Darkness Within Rune"));
        internalItemTranslations.put(SPIDER_CATALYST, Component.literal("Spider Catalyst"));
        internalItemTranslations.put(TARANTULA_SILK, Component.literal("Tarantula Silk"));
        internalItemTranslations.put(BANE_OF_ARTHROPODS_SIX, Component.literal("Bane Of Arthropods 6"));
        internalItemTranslations.put(TARANTULA_CATALYST, Component.literal("Tarantula Catalyst"));
        internalItemTranslations.put(FLY_SWATTER, Component.literal("Fly Swatter"));
        internalItemTranslations.put(VIAL_OF_VENOM, Component.literal("Vial of Venom"));
        internalItemTranslations.put(TARANTULA_TALISMAN, Component.literal("Tarantula Talisman"));
        internalItemTranslations.put(DIGESTED_MOSQUITO, Component.literal("Digested Mosquito"));
        internalItemTranslations.put(SHRIVELED_WASP, Component.literal("Shriveled Wasp"));
        internalItemTranslations.put(ENSNARED_SNAIL, Component.literal("Ensnared Snail"));
        internalItemTranslations.put(PRIMORDIAL_EYE, Component.literal("Primordial Eye"));
        internalItemTranslations.put(DYE_BRICK_RED, Component.literal("Brick Red Dye"));

        // wolf
        internalItemTranslations.put(WOLF_TOOTH, Component.literal("Wolf Tooth"));
        internalItemTranslations.put(HAMSTER_WHEEL, Component.literal("Hamster Wheel"));
        internalItemTranslations.put(SPIRIT_RUNE, Component.literal("◆ Spirit Rune"));
        internalItemTranslations.put(CRITICAL_SIX, Component.literal("Critical 6"));
        internalItemTranslations.put(FURBALL, Component.literal("Furball"));
        internalItemTranslations.put(RED_CLAW_EGG, Component.literal("Red Claw Egg"));
        internalItemTranslations.put(COUTURE_RUNE, Component.literal("◆ Couture Rune"));
        internalItemTranslations.put(OVERFLUX_CAPACITOR, Component.literal("Overflux Capacitor"));
        internalItemTranslations.put(GRIZZLY_BAIT, Component.literal("Grizzly Salmon"));
        internalItemTranslations.put(DYE_CELESTE, Component.literal("Celeste Dye"));

        // voidgloom
        internalItemTranslations.put(NULL_SPHERE, Component.literal("Null Sphere"));
        internalItemTranslations.put(TWILIGHT_ARROW_POISON, Component.literal("Twilight Arrow Poison"));
        internalItemTranslations.put(ENDERSNAKE_RUNE, Component.literal("◆ Endersnake Rune"));
        internalItemTranslations.put(SUMMONING_EYE, Component.literal("Summoning Eye"));
        internalItemTranslations.put(MANA_STEAL_ONE, Component.literal("Mana Steal 1"));
        internalItemTranslations.put(TRANSMISSION_TUNER, Component.literal("Transmission Tuner"));
        internalItemTranslations.put(NULL_ATOM, Component.literal("Null Atom"));
        internalItemTranslations.put(HAZMAT_ENDERMAN, Component.literal("Hazmat Enderman"));
        internalItemTranslations.put(POCKET_ESPRESSO_MACHINE, Component.literal("Pocket Espresso Machine"));
        internalItemTranslations.put(SMARTY_PANTS_ONE, Component.literal("Smarty Pants 1"));
        internalItemTranslations.put(END_RUNE, Component.literal("◆ End Rune"));
        internalItemTranslations.put(HANDY_BLOOD_CHALICE, Component.literal("Handy Blood Chalice"));
        internalItemTranslations.put(SINFUL_DICE, Component.literal("Sinful Dice"));
        internalItemTranslations.put(EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER, Component.literal("Exceedingly Rare Ender Artifact Upgrader"));
        internalItemTranslations.put(VOID_CONQUEROR_ENDERMAN_SKIN, Component.literal("Void Conqueror Enderman Skin"));
        internalItemTranslations.put(ETHERWARP_MERGER, Component.literal("Etherwarp Merger"));
        internalItemTranslations.put(JUDGEMENT_CORE, Component.literal("Judgement Core"));
        internalItemTranslations.put(ENCHANT_RUNE, Component.literal("◆ Enchant Rune"));
        internalItemTranslations.put(ENDER_SLAYER_SEVEN, Component.literal("Ender Slayer 7"));
        internalItemTranslations.put(DYE_BYZANTIUM, Component.literal("Byzantium Dye"));

        // inferno
        internalItemTranslations.put(DERELICT_ASHE, Component.literal("Derelict Ashe"));
        internalItemTranslations.put(ENCHANTED_BLAZE_POWDER, Component.literal("Enchanted Blaze Powder"));
        internalItemTranslations.put(LAVATEARS_RUNE, Component.literal("◆ Lavatears Rune"));
        internalItemTranslations.put(WISPS_ICE_FLAVORED_WATER, Component.literal("Wisp's Ice-Flavored Water"));
        internalItemTranslations.put(BUNDLE_OF_MAGMA_ARROWS, Component.literal("Bundle of Magma Arrows"));
        internalItemTranslations.put(MANA_DISINTEGRATOR, Component.literal("Mana Disintegrator"));
        internalItemTranslations.put(SCORCHED_BOOKS, Component.literal("Scorched Books"));
        internalItemTranslations.put(KELVIN_INVERTER, Component.literal("Kelvin Inverter"));
        internalItemTranslations.put(BLAZE_ROD_DISTILLATE, Component.literal("Blaze Rod Distillate"));
        internalItemTranslations.put(GLOWSTONE_DISTILLATE, Component.literal("Glowstone Distillate"));
        internalItemTranslations.put(MAGMA_CREAM_DISTILLATE, Component.literal("Magma Cream Distillate"));
        internalItemTranslations.put(NETHER_WART_DISTILLATE, Component.literal("Nether Wart Distillate"));
        internalItemTranslations.put(GABAGOOL_DISTILLATE, Component.literal("Gabagool Distillate"));
        internalItemTranslations.put(SCORCHED_POWER_CRYSTAL, Component.literal("Scorched Power Crystal"));
        internalItemTranslations.put(ARCHFIEND_DICE, Component.literal("Archfiend Dice"));
        internalItemTranslations.put(FIRE_ASPECT_THREE, Component.literal("Fire Aspect 3"));
        internalItemTranslations.put(FIERY_BURST_RUNE, Component.literal("◆ Fiery Burst Rune"));
        internalItemTranslations.put(FLAWED_OPAL_GEMSTONE, Component.literal("Flawed Opal Gemstone"));
        internalItemTranslations.put(DUPLEX, Component.literal("Duplex 1"));
        internalItemTranslations.put(HIGH_CLASS_ARCHFIEND_DICE, Component.literal("High Class Archfiend Dice"));
        internalItemTranslations.put(WILSON_ENGINEERING_PLANS, Component.literal("Wilson's Engineering Plans"));
        internalItemTranslations.put(SUBZERO_INVERTER, Component.literal("Subzero Inverter"));
        internalItemTranslations.put(DYE_FLAME, Component.literal("Flame Dye"));

        // rift
        internalItemTranslations.put(COVEN_SEAL, Component.literal("Coven Seal"));
        internalItemTranslations.put(QUANTUM_BOOK_BUNDLE, Component.literal("Quantum (Book Bundle)"));
        internalItemTranslations.put(SOULTWIST_RUNE, Component.literal("◆ Soultwist Rune"));
        internalItemTranslations.put(BUBBA_BLISTER, Component.literal("Bubba Blister"));
        internalItemTranslations.put(FANGTASTIC_CHOCOLATE_CHIP, Component.literal("Fang-Tastic Chocolate Chip"));
        internalItemTranslations.put(GUARDIAN_LUCKY_BLOCK, Component.literal("Guardian Lucky Block"));
        internalItemTranslations.put(MCGRUBBERS_BURGER, Component.literal("McGrubber's Burger"));
        internalItemTranslations.put(UNFANGED_VAMPIRE_PART, Component.literal("Unfanged Vampire Part"));
        internalItemTranslations.put(THE_ONE_BOOK_BUNDLE, Component.literal("The One (Book Bundle)"));
        internalItemTranslations.put(DYE_SANGRIA, Component.literal("Sangria Dye"));
    }

    public Component getDisplayName() {
        Component displayName = internalItemTranslations.get(this);

        if (displayName == null) {
            if (this.itemStack != null && this.itemStack != ItemStack.EMPTY) {
                Component nameComponent = this.itemStack.get(DataComponents.CUSTOM_NAME);
                if (nameComponent != null) {
                    displayName = nameComponent;
                }
            } else {
                displayName = Component.literal(this.name());
            }
        }

        return displayName;
    }

}