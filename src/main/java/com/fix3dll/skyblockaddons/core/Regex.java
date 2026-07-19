package com.fix3dll.skyblockaddons.core;

import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central registry for all regular expressions used throughout the mod.
 * <p>
 * Patterns are loaded from {@code resources/regex.json} at startup and compiled once.
 * Each constant is thread-safe and can be updated at runtime without a restart.
 */
public enum Regex {
    VARIABLE_PATTERN,
    VERSION_PATTERN,
    FROM_TO_PATTERN,
    PET_LEVEL_PATTERN,
    FAVORITE_PATTERN,
    TRACKED_ANIMAL_NAME_PATTERN,
    TREVOR_FIND_ANIMAL_PATTERN,
    ANIMAL_DIED_PATTERN,
    ANIMAL_KILLED_PATTERN,
    BACKPACK_STORAGE_PATTERN,
    ENDERCHEST_STORAGE_PATTERN,
    ITEM_COOLDOWN_PATTERN,
    ALTERNATE_COOLDOWN_PATTERN,
    DEPLOYABLE_PATTERN,
    TOTEM_PATTERN,
    PATTERN_MILESTONE,
    PATTERN_COLLECTED_ESSENCES,
    PATTERN_BONUS_ESSENCE,
    PATTERN_SALVAGE_ESSENCES,
    PATTERN_SECRETS,
    PATTERN_PLAYER_LINE,
    PATTERN_PLAYER_LIST_INFO_DEATHS,
    PATTERN_STRIP_FORMAT,
    COIN_COST_PATTERN,
    ENCHANTMENT_PATTERN,
    CANDY_PATTERN,
    CANDY_FORMATTED_PATTERN,
    GOD_POTION_PATTERN,
    ACTIVE_EFFECTS_PATTERN,
    EFFECT_COUNT_PATTERN,
    COOKIE_BUFF_PATTERN,
    UPGRADES_PATTERN,
    RAIN_TIME_PATTERN,
    SKILL_LEVEL_PATTERN,
    OLD_SKILL_LEVEL_PATTERN,
    JERRY_POWER_UPS_PATTERN,
    USERNAME_TAB_PATTERN,
    NO_ARROWS_LEFT_PATTERN,
    ONLY_HAVE_ARROWS_LEFT_PATTERN,
    ABILITY_CHAT_PATTERN,
    PROFILE_CHAT_PATTERN,
    SWITCH_PROFILE_CHAT_PATTERN,
    MINION_CANT_REACH_PATTERN,
    DRAGON_KILLED_PATTERN,
    DRAGON_SPAWNED_PATTERN,
    SLAYER_COMPLETED_PATTERN,
    SLAYER_COMPLETED_PATTERN_AUTO1,
    SLAYER_COMPLETED_PATTERN_AUTO2,
    DEATH_MESSAGE_PATTERN,
    REVIVE_MESSAGE_PATTERN,
    NEXT_TIER_PET_PROGRESS,
    MAXED_TIER_PET_PROGRESS,
    SPIRIT_SCEPTRE_MESSAGE_PATTERN,
    PROFILE_TYPE_SYMBOL,
    NETHER_FACTION_SYMBOL,
    AUTOPET_PATTERN,
    PET_LEVELED_UP_PATTERN,
    PET_ITEM_PATTERN,
    PET_CUSTOM_NAME_PATTERN,
    ESSENCE_NAME_PATTERN,
    ATTRIBUTE_SHARD_NAME_PATTERN,
    DUNGEON_STAR_PATTERN,
    SKILL_GAIN_PATTERN_S,
    MANA_PATTERN_S,
    DEFENSE_PATTERN_S,
    HEALTH_PATTERN_S,
    VITALITY_PATTERN_S,
    SLAYER_ARMOR_STACK_PATTERN,
    ITEM_TYPE_AND_RARITY_PATTERN,
    BACKPACK_SLOT_PATTERN,
    NUMERAL_VALIDATION_PATTERN,
    NUMERAL_FINDING_PATTERN,
    WORD_PART_PATTERN,
    STRIP_COLOR_PATTERN,
    STRIP_ICONS_PATTERN,
    STRIP_PREFIX_PATTERN,
    STRIP_PET_DISPLAY_NAME,
    REPEATED_COLOR_PATTERN,
    NUMBERS_SLASHES,
    SCOREBOARD_CHARACTERS,
    FLOAT_CHARACTERS,
    INTEGER_CHARACTERS,
    TRIM_WHITESPACE_RESETS,
    USERNAME_PATTERN,
    MAGNITUDE_PATTERN,
    TEXTURE_URL_PATTERN,
    QUANTITY_PATTERN,
    SERVER_REGEX,
    SLAYER_TYPE_REGEX,
    SLAYER_PROGRESS_REGEX,
    NEXT_PERK_DATE_PATTERN,
    SIDEBAR_DATE_PATTERN,
    SIDEBAR_TIME_PATTERN,
    PEST_PATTERN;

    /** The compiled pattern for this entry. */
    @Setter private volatile Pattern pattern;

    /**
     * Returns the compiled pattern for this entry.
     * @return the compiled {@link Pattern}
     * @throws IllegalStateException if the pattern has not been loaded yet
     */
    public Pattern get() {
        Pattern p = this.pattern;
        if (p == null) {
            throw new IllegalStateException("Regex pattern has not been initialized or failed to load: " + this.name());
        }
        return p;
    }

    /**
     * Creates a {@link Matcher} for the given input against this pattern.
     * @param input the character sequence to match
     * @return a new {@link Matcher}
     */
    public Matcher matcher(CharSequence input) {
        return get().matcher(input);
    }

}