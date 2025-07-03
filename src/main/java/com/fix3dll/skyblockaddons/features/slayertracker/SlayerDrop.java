package com.fix3dll.skyblockaddons.features.slayertracker;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;

@Getter
public enum SlayerDrop {

    // Revenant Drops
    REVENANT_FLESH(Items.ROTTEN_FLESH, "Revenant Flesh", "REVENANT_FLESH", SkyblockRarity.UNCOMMON, true),
    FOUL_FLESH(Items.COAL, "Foul Flesh", "FOUL_FLESH", SkyblockRarity.RARE),
    PESTILENCE_RUNE("PESTILENCE_RUNE", "ZOMBIE_SLAYER", SkyblockRarity.RARE),
    UNDEAD_CATALYST("UNDEAD_CATALYST", SkyblockRarity.RARE),
    SMITE_SIX("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "smite", 6),
    BEHEADED_HORROR("BEHEADED_HORROR", SkyblockRarity.EPIC),
    REVENANT_CATALYST("REVENANT_CATALYST", SkyblockRarity.EPIC),
    SNAKE_RUNE("SNAKE_RUNE", "SNAKE", SkyblockRarity.LEGENDARY),
    SCYTHE_BLADE(Items.DIAMOND, "Scythe Blade", "SCYTHE_BLADE", SkyblockRarity.LEGENDARY, true),
    SMITE_SEVEN("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.EPIC, "smite", 7),
    REVENANT_VISCERA(Items.COOKED_PORKCHOP, "Revenant Viscera", "REVENANT_VISCERA", SkyblockRarity.RARE, true),
    SHARD_OF_SHREDDED("SHARD_OF_THE_SHREDDED", SkyblockRarity.LEGENDARY),
    WARDEN_HEART("WARDEN_HEART", SkyblockRarity.LEGENDARY),

    // Tarantula Drops
    TARANTULA_WEB(Items.STRING, "Tarantula Web", "TARANTULA_WEB", SkyblockRarity.UNCOMMON, true),
    TOXIC_ARROW_POISON(Items.LIME_DYE, "Toxic Arrow Poison", "TOXIC_ARROW_POISON", SkyblockRarity.RARE),
    SPIDER_CATALYST("SPIDER_CATALYST", SkyblockRarity.RARE),
    BANE_OF_ARTHROPODS_SIX("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "bane_of_arthropods", 6),
    BITE_RUNE("BITE_RUNE", "BITE", SkyblockRarity.EPIC),
    FLY_SWATTER(Items.GOLDEN_SHOVEL, "Fly Swatter", "FLY_SWATTER", SkyblockRarity.EPIC, true),
    TARANTULA_TALISMAN("TARANTULA_TALISMAN", SkyblockRarity.EPIC),
    DIGESTED_MOSQUITO(Items.ROTTEN_FLESH, "Digested Mosquito", "DIGESTED_MOSQUITO", SkyblockRarity.LEGENDARY),

    // Sven Drops
    WOLF_TOOTH(Items.GHAST_TEAR, "Wolf Tooth", "WOLF_TOOTH", SkyblockRarity.UNCOMMON, true),
    HAMSTER_WHEEL(Blocks.OAK_TRAPDOOR.asItem(), "Hamster Wheel", "HAMSTER_WHEEL", SkyblockRarity.RARE, true),
    SPIRIT_RUNE("SPIRIT_RUNE", "SPIRIT", SkyblockRarity.RARE),
    FURBALL("FURBALL", SkyblockRarity.RARE),
    CRITICAL_SIX("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "critical", 6),
    RED_CLAW_EGG(Items.MOOSHROOM_SPAWN_EGG, "Red Claw Egg", "RED_CLAW_EGG", SkyblockRarity.EPIC),
    COUTURE_RUNE("COUTURE_RUNE", "COUTURE", SkyblockRarity.LEGENDARY),
    OVERFLUX_CAPACITOR(Items.QUARTZ, "Overflux Capacitor", "OVERFLUX_CAPACITOR", SkyblockRarity.EPIC),
    GRIZZLY_BAIT(Items.COD, "Grizzly Bait", "GRIZZLY_BAIT", SkyblockRarity.RARE),

    // Enderman Drops
    NULL_SPHERE(Items.FIREWORK_STAR, "Null Sphere", "NULL_SPHERE", SkyblockRarity.UNCOMMON, true),
    TWILIGHT_ARROW_POISON(Items.PURPLE_DYE, "Twilight Arrow Poison", "TWILIGHT_ARROW_POISON", SkyblockRarity.UNCOMMON),
    ENDERSNAKE_RUNE("ENDERSNAKE_RUNE", "ENDERSNAKE", SkyblockRarity.LEGENDARY),
    SUMMONING_EYE("SUMMONING_EYE", SkyblockRarity.EPIC),
    MANA_STEAL_ONE("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "mana_steal", 1),
    TRANSMISSION_TUNER("TRANSMISSION_TUNER", SkyblockRarity.EPIC),
    NULL_ATOM(Blocks.OAK_BUTTON.asItem(), "Null Atom", "NULL_ATOM", SkyblockRarity.RARE, true),
    HAZMAT_ENDERMAN("HAZMAT_ENDERMAN", SkyblockRarity.LEGENDARY),
    POCKET_ESPRESSO_MACHINE("POCKET_ESPRESSO_MACHINE", SkyblockRarity.COMMON),
    SMARTY_PANTS_ONE("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "smarty_pants", 1),
    END_RUNE("END_RUNE", "ENDERSNAKE", SkyblockRarity.EPIC),
    HANDY_BLOOD_CHALICE("HANDY_BLOOD_CHALICE", SkyblockRarity.COMMON),
    SINFUL_DICE("SINFUL_DICE", SkyblockRarity.EPIC),
    EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER("EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER", SkyblockRarity.LEGENDARY),
    VOID_CONQUEROR_ENDERMAN_SKIN("PET_SKIN_ENDERMAN_SLAYER", SkyblockRarity.EPIC),
    ETHERWARP_MERGER("ETHERWARP_MERGER", SkyblockRarity.EPIC),
    JUDGEMENT_CORE("JUDGEMENT_CORE", SkyblockRarity.LEGENDARY),
    ENCHANT_RUNE("ENCHANT_RUNE", "ENCHANT", SkyblockRarity.LEGENDARY),
    ENDER_SLAYER_SEVEN("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.RARE, "ender_slayer", 7),

    // Blaze Drops
    DERELICT_ASHE(Items.GUNPOWDER, "Derelict Ashe", "DERELICT_ASHE", SkyblockRarity.UNCOMMON, true),
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
    FIRE_ASPECT_THREE("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.COMMON, "fire_aspect", 3),
    FIERY_BURST_RUNE("FIERY_BURST_RUNE", "FIERY_BURST", SkyblockRarity.LEGENDARY),
    FLAWED_OPAL_GEMSTONE("FLAWED_OPAL_GEM", SkyblockRarity.UNCOMMON),
    DUPLEX("Enchanted Book", "ENCHANTED_BOOK", SkyblockRarity.COMMON, "ultimate_reiterate", 1),
    HIGH_CLASS_ARCHFIEND_DICE("HIGH_CLASS_ARCHFIEND_DICE", SkyblockRarity.LEGENDARY),
    WILSON_ENGINEERING_PLANS(Items.PAPER, "Wilson's Engineering Plans", "WILSON_ENGINEERING_PLANS", SkyblockRarity.LEGENDARY, true),
    SUBZERO_INVERTER("SUBZERO_INVERTER", SkyblockRarity.LEGENDARY),

    //Vampire Slayer
    COVEN_SEAL(Items.NETHER_WART, "Coven Seal", "COVEN_SEAL", SkyblockRarity.UNCOMMON),
    QUANTUM_BOOK_BUNDLE("ENCHANTED_BOOK_BUNDLE_QUANTUM", SkyblockRarity.UNCOMMON),
    SOULTWIST_RUNE("SOULTWIST_RUNE", "SOULTWIST", SkyblockRarity.EPIC),
    BUBBA_BLISTER("BUBBA_BLISTER", SkyblockRarity.LEGENDARY),
    FANGTASTIC_CHOCOLATE_CHIP(Items.COOKIE, "Fang-tastic Chocolate Chip", "CHOCOLATE_CHIP", SkyblockRarity.LEGENDARY),
    GUARDIAN_LUCKY_BLOCK("GUARDIAN_LUCKY_BLOCK", SkyblockRarity.LEGENDARY),
    MCGRUBBERS_BURGER("MCGRUBBER_BURGER", SkyblockRarity.EPIC), /* old */
    UNFANGED_VAMPIRE_PART("UNFANGED_VAMPIRE_PART", SkyblockRarity.LEGENDARY), /* old */
    THE_ONE_BOOK_BUNDLE("ENCHANTED_BOOK_BUNDLE_THE_ONE", SkyblockRarity.LEGENDARY); /* old */

    private final String skyblockID;
    private final SkyblockRarity rarity;
    private final ItemStack itemStack;
    private String runeID;

    /**
     * Creates an enchanted book slayer drop with a display name, skyblock id, item rarity,
     * skyblock enchant name, and enchant level.
     */
    SlayerDrop(String name, String skyblockID, SkyblockRarity rarity, String enchantID, int enchantLevel) {
        this.itemStack = ItemUtils.createEnchantedBook(name, skyblockID, enchantID, enchantLevel);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, and item rarity
     */
    SlayerDrop(Item item, String name, String skyblockID, SkyblockRarity rarity) {
        this(item, name, skyblockID, rarity, false);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, item rarity, and enchanted state
     */
    SlayerDrop(Item item, String name, String skyblockID, SkyblockRarity rarity, boolean enchanted) {
        this.itemStack = ItemUtils.createItemStack(item, name, skyblockID, enchanted);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     *Get textured skull from {@link ItemUtils#getTexturedHead(String)} with skyblockId
     */
    SlayerDrop(String skyblockID, SkyblockRarity rarity) {
        this.itemStack = ItemUtils.getTexturedHead(skyblockID);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Get textured skull from {@link ItemUtils#getTexturedHead(String)} with identifier and runeId field
     */
    SlayerDrop(String identifier, String runeID, SkyblockRarity rarity) {
        this(identifier, rarity);
        this.runeID = runeID;
    }

    private static final HashMap<String, String> internalItemTranslations = new HashMap<>();

    static {
        internalItemTranslations.put("bossesKilled", "Bosses Killed");

        // revenant
        internalItemTranslations.put("revenantFlesh", "Revenant Flesh");
        internalItemTranslations.put("foulFlesh", "Foul Flesh");
        internalItemTranslations.put("pestilenceRune", "Pestilence Rune");
        internalItemTranslations.put("undeadCatalyst", "Undead Catalyst");
        internalItemTranslations.put("smiteSix", "Smite 6");
        internalItemTranslations.put("beheadedHorror", "Beheaded Horror");
        internalItemTranslations.put("revenantCatalyst", "Revenant Catalyst");
        internalItemTranslations.put("snakeRune", "Snake Rune");
        internalItemTranslations.put("scytheBlade", "Scythe Blade");
        internalItemTranslations.put("revenantViscera", "Revenant Viscera");
        internalItemTranslations.put("smiteSeven", "Smite 7");
        internalItemTranslations.put("shardOfShredded", "Shard of Shredded");
        internalItemTranslations.put("wardenHeart", "Warden Heart");

        // tarantula
        internalItemTranslations.put("tarantulaWeb", "Tarantula Web");
        internalItemTranslations.put("toxicArrowPoison", "Toxic Arrow Poison");
        internalItemTranslations.put("spiderCatalyst", "Spider Catalyst");
        internalItemTranslations.put("baneOfArthropodsSix", "Bane Of Arthropods 6");
        internalItemTranslations.put("biteRune", "Bite Rune");
        internalItemTranslations.put("flySwatter", "Fly Swatter");
        internalItemTranslations.put("tarantulaTalisman", "Tarantula Talisman");
        internalItemTranslations.put("digestedMosquito", "Digested Mosquito");

        // wolf
        internalItemTranslations.put("wolfTooth", "Wolf Tooth");
        internalItemTranslations.put("hamsterWheel", "Hamster Wheel");
        internalItemTranslations.put("spiritRune", "Spirit Rune");
        internalItemTranslations.put("criticalSix", "Critical 6");
        internalItemTranslations.put("furball", "Furball");
        internalItemTranslations.put("redClawEgg", "Red Claw Egg");
        internalItemTranslations.put("coutureRune", "Couture Rune");
        internalItemTranslations.put("grizzlyBait", "Grizzly Bait");
        internalItemTranslations.put("overfluxCapacitor", "Overflux Capacitor");

        // voidgloom
        internalItemTranslations.put("nullSphere", "Null Sphere");
        internalItemTranslations.put("twilightArrowPoison", "Twilight Arrow Poison");
        internalItemTranslations.put("endersnakeRune", "Endersnake Rune");
        internalItemTranslations.put("summoningEye", "Summoning Eye");
        internalItemTranslations.put("manaStealOne", "Mana Steal 1");
        internalItemTranslations.put("transmissionTuner", "Transmission Tuner");
        internalItemTranslations.put("nullAtom", "Null Atom");
        internalItemTranslations.put("hazmatEnderman", "Hazmat Enderman");
        internalItemTranslations.put("pocketEspressoMachine", "Pocket Espresso Machine");
        internalItemTranslations.put("smartyPantsOne", "Smarty Pants 1");
        internalItemTranslations.put("endRune", "End Rune");
        internalItemTranslations.put("handyBloodChalice", "Handy Blood Chalice");
        internalItemTranslations.put("sinfulDice", "Sinful Dice");
        internalItemTranslations.put("exceedinglyRareEnderArtifactUpgrader", "Exceedingly Rare Ender Artifact Upgrader");
        internalItemTranslations.put("voidConquerorEndermanSkin", "Void Conqueror Enderman Skin");
        internalItemTranslations.put("etherwarpMerger", "Etherwarp Merger");
        internalItemTranslations.put("judgementCore", "Judgement Core");
        internalItemTranslations.put("enchantRune", "Enchant Rune");
        internalItemTranslations.put("enderSlayerSeven", "Ender Slayer 7");

        // inferno
        internalItemTranslations.put("derelictAshe", "Derelict Ashe");
        internalItemTranslations.put("lavatearsRune", "Lavatears Rune");
        internalItemTranslations.put("wispsIceFlavoredWater", "Wisp's Ice-Flavored Water");
        internalItemTranslations.put("bundleOfMagmaArrows", "Bundle of Magma Arrows");
        internalItemTranslations.put("manaDisintegrator", "Mana Disintegrator");
        internalItemTranslations.put("scorchedBooks", "Scorched Books");
        internalItemTranslations.put("kelvinInverter", "Kelvin Inverter");
        internalItemTranslations.put("blazeRodDistillate", "Blaze Rod Distillate");
        internalItemTranslations.put("glowstoneDistillate", "Glowstone Distillate");
        internalItemTranslations.put("magmaCreamDistillate", "Magma Cream Distillate");
        internalItemTranslations.put("netherWartDistillate", "Nether Wart Distillate");
        internalItemTranslations.put("gabagoolDistillate", "Gabagool Distillate");
        internalItemTranslations.put("scorchedPowerCrystal", "Scorched Power Crystal");
        internalItemTranslations.put("archfiendDice", "Archfiend Dice");
        internalItemTranslations.put("fireAspectThree", "Fire Aspect 3");
        internalItemTranslations.put("fieryBurstRune", "Fiery Burst Rune");
        internalItemTranslations.put("flawedOpalGemstone", "Flawed Opal Gemstone");
        internalItemTranslations.put("duplex", "Duplex 1");
        internalItemTranslations.put("highClassArchfiendDice", "High Class Archfiend Dice");
        internalItemTranslations.put("wilsonEngineeringPlans", "Wilson's Engineering Plans");
        internalItemTranslations.put("subzeroInverter", "Subzero Inverter");

        // rift
        internalItemTranslations.put("covenSeal", "Coven Seal");
        internalItemTranslations.put("quantumBookBundle", "Quantum (Book Bundle)");
        internalItemTranslations.put("soultwistRune", "Soultwist Rune");
        internalItemTranslations.put("bubbaBlister", "Bubba Blister");
        internalItemTranslations.put("fangtasticChocolateChip", "Fang-Tastic Chocolate Chip");
        internalItemTranslations.put("guardianLuckyBlock", "Guardian Lucky Block");
        internalItemTranslations.put("mcgrubbersBurger", "McGrubber's Burger");
        internalItemTranslations.put("unfangedVampirePart", "Unfanged Vampire Part");
        internalItemTranslations.put("theOneBookBundle", "The One (Book Bundle)");
    }

    public String getDisplayName() {
        return internalItemTranslations.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}
