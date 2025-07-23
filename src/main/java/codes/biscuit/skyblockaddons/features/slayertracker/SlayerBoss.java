package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

import static codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop.*;

@Getter
public enum SlayerBoss {

    REVENANT("Zombie", REVENANT_FLESH, FOUL_FLESH, REVENANT_SHARD, PESTILENCE_RUNE, UNDEAD_CATALYST, SMITE_SIX,
            BEHEADED_HORROR, REVENANT_CATALYST, SNAKE_RUNE, FESTERING_MAGGOT, REVENANT_VISCERA, SCYTHE_BLADE, SMITE_SEVEN,
            SHARD_OF_SHREDDED, WARDEN_HEART, DYE_MATCHA),

    TARANTULA("Spider", TARANTULA_WEB, TOXIC_ARROW_POISON, BITE_RUNE, DARKNESS_WITHIN_RUNE, SPIDER_CATALYST,
            TARANTULA_SILK, BANE_OF_ARTHROPODS_SIX, TARANTULA_CATALYST, FLY_SWATTER, VIAL_OF_VENOM, TARANTULA_TALISMAN,
            DIGESTED_MOSQUITO, SHRIVELED_WASP, ENSNARED_SNAIL, PRIMORDIAL_EYE, DYE_BRICK_RED),

    SVEN("Wolf", WOLF_TOOTH, HAMSTER_WHEEL, SPIRIT_RUNE, CRITICAL_SIX, FURBALL, RED_CLAW_EGG, COUTURE_RUNE,
            GRIZZLY_BAIT, OVERFLUX_CAPACITOR, DYE_CELESTE),

    VOIDGLOOM("Enderman", NULL_SPHERE, TWILIGHT_ARROW_POISON, ENDERSNAKE_RUNE, SUMMONING_EYE, MANA_STEAL_ONE,
            TRANSMISSION_TUNER, NULL_ATOM, HAZMAT_ENDERMAN, POCKET_ESPRESSO_MACHINE, SMARTY_PANTS_ONE, END_RUNE,
            HANDY_BLOOD_CHALICE, SINFUL_DICE, EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER, VOID_CONQUEROR_ENDERMAN_SKIN,
            ETHERWARP_MERGER, JUDGEMENT_CORE, ENCHANT_RUNE, ENDER_SLAYER_SEVEN, DYE_BYZANTIUM),

    INFERNO("Blaze", DERELICT_ASHE, ENCHANTED_BLAZE_POWDER, LAVATEARS_RUNE, WISPS_ICE_FLAVORED_WATER,
            BUNDLE_OF_MAGMA_ARROWS, MANA_DISINTEGRATOR, SCORCHED_BOOKS, KELVIN_INVERTER, BLAZE_ROD_DISTILLATE,
            GLOWSTONE_DISTILLATE, MAGMA_CREAM_DISTILLATE, NETHER_WART_DISTILLATE, GABAGOOL_DISTILLATE,
            SCORCHED_POWER_CRYSTAL, ARCHFIEND_DICE, FIRE_ASPECT_THREE, FIERY_BURST_RUNE, FLAWED_OPAL_GEMSTONE, DUPLEX,
            HIGH_CLASS_ARCHFIEND_DICE, WILSON_ENGINEERING_PLANS, SUBZERO_INVERTER, DYE_FLAME),

    RIFTSTALKER("Vampire", COVEN_SEAL, QUANTUM_BOOK_BUNDLE, SOULTWIST_RUNE, BUBBA_BLISTER, FANGTASTIC_CHOCOLATE_CHIP,
            GUARDIAN_LUCKY_BLOCK, MCGRUBBERS_BURGER, UNFANGED_VAMPIRE_PART, THE_ONE_BOOK_BUNDLE, DYE_SANGRIA);

    private final List<SlayerDrop> drops;
    private final String mobType;

    SlayerBoss(String mobType, SlayerDrop... drops) {
        this.mobType = mobType;
        this.drops = Lists.newArrayList(drops);
    }

    public static SlayerBoss getFromMobType(String mobType) {
        for (SlayerBoss slayerBoss : SlayerBoss.values()) {
            if (slayerBoss.mobType.equalsIgnoreCase(mobType)) {
                return slayerBoss;
            }
        }

        return null;
    }

    public String getDisplayName() {
        return Translations.getMessage("slayerTracker." + this.name().toLowerCase(Locale.US));
    }

}