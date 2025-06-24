package com.fix3dll.skyblockaddons.features.slayertracker;

import com.fix3dll.skyblockaddons.core.Translations;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;

import static com.fix3dll.skyblockaddons.features.slayertracker.SlayerDrop.*;

@Getter
public enum SlayerBoss {

    REVENANT("Zombie", REVENANT_FLESH, FOUL_FLESH, PESTILENCE_RUNE, UNDEAD_CATALYST, SMITE_SIX, BEHEADED_HORROR,
            REVENANT_CATALYST, SNAKE_RUNE, SCYTHE_BLADE, REVENANT_VISCERA, SMITE_SEVEN, SHARD_OF_SHREDDED, WARDEN_HEART),

    TARANTULA("Spider", TARANTULA_WEB, TOXIC_ARROW_POISON, SPIDER_CATALYST, BANE_OF_ARTHROPODS_SIX, BITE_RUNE,
            FLY_SWATTER, TARANTULA_TALISMAN, DIGESTED_MOSQUITO),

    SVEN("Wolf", WOLF_TOOTH, HAMSTER_WHEEL, SPIRIT_RUNE, CRITICAL_SIX, FURBALL, RED_CLAW_EGG, COUTURE_RUNE,
            GRIZZLY_BAIT, OVERFLUX_CAPACITOR),

    VOIDGLOOM("Enderman", NULL_SPHERE, TWILIGHT_ARROW_POISON, ENDERSNAKE_RUNE, SUMMONING_EYE, MANA_STEAL_ONE,
            TRANSMISSION_TUNER, NULL_ATOM, HAZMAT_ENDERMAN, POCKET_ESPRESSO_MACHINE, SMARTY_PANTS_ONE, END_RUNE,
            HANDY_BLOOD_CHALICE, SINFUL_DICE, EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER, VOID_CONQUEROR_ENDERMAN_SKIN,
            ETHERWARP_MERGER, JUDGEMENT_CORE, ENCHANT_RUNE, ENDER_SLAYER_SEVEN),

    INFERNO("Blaze", DERELICT_ASHE, LAVATEARS_RUNE, WISPS_ICE_FLAVORED_WATER, BUNDLE_OF_MAGMA_ARROWS,
            MANA_DISINTEGRATOR, SCORCHED_BOOKS, KELVIN_INVERTER, BLAZE_ROD_DISTILLATE, GLOWSTONE_DISTILLATE,
            MAGMA_CREAM_DISTILLATE, NETHER_WART_DISTILLATE, GABAGOOL_DISTILLATE, SCORCHED_POWER_CRYSTAL, ARCHFIEND_DICE,
            FIRE_ASPECT_THREE, FIERY_BURST_RUNE, FLAWED_OPAL_GEMSTONE, DUPLEX, HIGH_CLASS_ARCHFIEND_DICE,
            WILSON_ENGINEERING_PLANS, SUBZERO_INVERTER),

    RIFTSTALKER("Vampire", COVEN_SEAL, QUANTUM_BOOK_BUNDLE, SOULTWIST_RUNE, BUBBA_BLISTER, FANGTASTIC_CHOCOLATE_CHIP,
            GUARDIAN_LUCKY_BLOCK, MCGRUBBERS_BURGER, UNFANGED_VAMPIRE_PART, THE_ONE_BOOK_BUNDLE);

    private final EnumSet<SlayerDrop> drops;
    private final String mobType;

    SlayerBoss(String mobType, SlayerDrop... drops) {
        this.mobType = mobType;
        this.drops = EnumSet.copyOf(Arrays.asList(drops));
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
