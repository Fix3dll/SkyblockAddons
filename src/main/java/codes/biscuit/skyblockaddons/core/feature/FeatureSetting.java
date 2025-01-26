package codes.biscuit.skyblockaddons.core.feature;

import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * There are 3 types of FeatureSettings: Universal, standard, and those with dynamic (unspecified) translation.
 * <ol>
 *     <li>Universal setting: We can think of universal settings as Dummy FeatureSetting. Although it is an enum like
 *     other settings, it does not have a fixed setting that it is associated with. It can be associated with a Feature
 *     according to special conditions.</li>
 *     <li>Standard setting: This is the default. Its value is preserved in the feature map in FeatureData.</li>
 *     <li>Setting with dynamic translation: The only difference from the standard setting is that the translation value
 *     is null. The translation may change depending on the condition.</li>
 * </ol>
 * Also, the display order in the {@link SettingsGui} depends on the ordinal order.
 * @implNote Please don't forget to add new Feature or FeatureSetting values to {@code defaults.json} before push.
 */
public enum FeatureSetting {
    COLOR("settings.changeColor"),
    X_ALLIGNMENT("settings.xAllignment"),

    DARK_AUCTION_TIMER_IN_OTHER_GAMES("settings.showInOtherGames", Feature.DARK_AUCTION_TIMER),
    FARM_EVENT_TIMER_IN_OTHER_GAMES("settings.showInOtherGames", Feature.FARM_EVENT_TIMER),
    DROP_CONFIRMATION_IN_OTHER_GAMES("settings.showInOtherGames", Feature.DROP_CONFIRMATION),
    OUTBID_ALERT_SOUND_IN_OTHER_GAMES("settings.showInOtherGames", Feature.OUTBID_ALERT_SOUND),
    REPEATING_FULL_INVENTORY_WARNING("settings.repeating", Feature.FULL_INVENTORY_WARNING),
    REPEATING_BOSS_APPROACH_ALERT("settings.repeating", Feature.BOSS_APPROACH_ALERT),
    REVENANT_TRACKER_TEXT_MODE("settings.textMode", Feature.REVENANT_SLAYER_TRACKER),
    TARANTULA_TRACKER_TEXT_MODE("settings.textMode", Feature.TARANTULA_SLAYER_TRACKER),
    SVEN_TRACKER_TEXT_MODE("settings.textMode", Feature.SVEN_SLAYER_TRACKER),
    VOIDGLOOM_TRACKER_TEXT_MODE("settings.textMode", Feature.VOIDGLOOM_SLAYER_TRACKER),
    RIFTSTALKER_TRACKER_TEXT_MODE("settings.textMode", Feature.RIFTSTALKER_SLAYER_TRACKER),
    INFERNO_TRACKER_TEXT_MODE("settings.textMode", Feature.INFERNO_SLAYER_TRACKER),
    DRAGON_TRACKER_TEXT_MODE("settings.textMode", Feature.DRAGON_STATS_TRACKER),
    REVENANT_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.REVENANT_SLAYER_TRACKER),
    TARANTULA_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.TARANTULA_SLAYER_TRACKER),
    SVEN_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.SVEN_SLAYER_TRACKER),
    VOIDGLOOM_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.VOIDGLOOM_SLAYER_TRACKER),
    RIFTSTALKER_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.RIFTSTALKER_SLAYER_TRACKER),
    INFERNO_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.INFERNO_SLAYER_TRACKER),
    DRAGON_TRACKER_COLOR_BY_RARITY("settings.colorByRarity", Feature.DRAGON_STATS_TRACKER),
    BASE_STAT_COLOR_BY_RARITY("settings.colorByRarity", Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE),
    COUNTER_ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", Feature.ZEALOT_COUNTER),
    TOTAL_ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", Feature.SHOW_TOTAL_ZEALOT_COUNT),
    EYE_ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", Feature.SHOW_SUMMONING_EYE_COUNT),
    AVERAGE_ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE),

    BACKPACK_STYLE("settings.backpackStyle", Feature.SHOW_BACKPACK_PREVIEW),
    DEPLOYABLE_DISPLAY_STYLE("settings.deployableDisplayStyle", Feature.DEPLOYABLE_STATUS_DISPLAY),
    PET_ITEM_STYLE("settings.petItemStyle", Feature.PET_DISPLAY),
    DRAGONS_NEST_ONLY("settings.dragonsNestOnly", Feature.DRAGON_STATS_TRACKER),
    USE_VANILLA_TEXTURE("settings.useVanillaTexture", Feature.DEFENCE_ICON),
    SHOW_ONLY_WHEN_HOLDING_SHIFT("settings.showOnlyWhenHoldingShift", Feature.SHOW_BACKPACK_PREVIEW),
    MAKE_INVENTORY_COLORED("settings.makeBackpackInventoriesColored", Feature.SHOW_BACKPACK_PREVIEW),
    CHANGE_BAR_COLOR_WITH_POTIONS("settings.changeBarColorForPotions", Feature.HEALTH_BAR),
    CAKE_BAG_PREVIEW("settings.showCakeBagPreview", Feature.SHOW_BACKPACK_PREVIEW),
    ROTATE_MAP("settings.rotateMap", Feature.DUNGEONS_MAP_DISPLAY),
    CENTER_ROTATION_ON_PLAYER("settings.centerRotationOnYourPlayer", Feature.DUNGEONS_MAP_DISPLAY),
    SHOW_PLAYER_HEADS_ON_MAP("settings.showPlayerHeadsOnMap", Feature.DUNGEONS_MAP_DISPLAY),
    SHOW_GLOWING_ITEMS_ON_ISLAND("settings.showGlowingItemsOnIsland", Feature.MAKE_DROPPED_ITEMS_GLOW),
    SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL("settings.skillActionsLeftUntilNextLevel", Feature.SKILL_DISPLAY),
    HIDE_WHEN_NOT_IN_CRYPTS("settings.hideWhenNotDoingQuest", Feature.REVENANT_SLAYER_TRACKER),
    HIDE_WHEN_NOT_IN_SPIDERS_DEN("settings.hideWhenNotDoingQuest", Feature.TARANTULA_SLAYER_TRACKER),
    HIDE_WHEN_NOT_IN_CASTLE("settings.hideWhenNotDoingQuest", Feature.SVEN_SLAYER_TRACKER),
    PERSONAL_COMPACTOR_PREVIEW("settings.showPersonalCompactorPreview", Feature.SHOW_BACKPACK_PREVIEW),
    SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP("settings.showSkillPercentageInstead", Feature.SKILL_DISPLAY),
    SHOW_SKILL_XP_GAINED("settings.showSkillXPGained", Feature.SKILL_DISPLAY),
    SHOW_SALVAGE_ESSENCES_COUNTER("settings.showSalvageEssencesCounter", Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY),
    HEALING_CIRCLE_OPACITY("settings.healingCircleOpacity", Feature.SHOW_HEALING_CIRCLE_WALL),
    BIGGER_WAKE("settings.biggerWake", Feature.FISHING_PARTICLE_OVERLAY),
    HIGHLIGHT_ENCHANTMENTS("settings.highlightSpecialEnchantments", Feature.ENCHANTMENT_LORE_PARSING),
    PERFECT_ENCHANT_COLOR("enchants.superTier", Feature.ENCHANTMENT_LORE_PARSING),
    GREAT_ENCHANT_COLOR("enchants.highTier", Feature.ENCHANTMENT_LORE_PARSING),
    GOOD_ENCHANT_COLOR("enchants.midTier", Feature.ENCHANTMENT_LORE_PARSING),
    POOR_ENCHANT_COLOR("enchants.lowTier", Feature.ENCHANTMENT_LORE_PARSING),
    COMMA_ENCHANT_COLOR("enchants.commas", Feature.ENCHANTMENT_LORE_PARSING),
    HIDE_ENCHANTMENT_LORE("settings.hideEnchantDescription", Feature.ENCHANTMENT_LORE_PARSING),
    HIDE_GREY_ENCHANTS("settings.hideGreyEnchants", Feature.ENCHANTMENT_LORE_PARSING),
    ENCHANT_LAYOUT("enchantLayout.title", Feature.ENCHANTMENT_LORE_PARSING),
    TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR("settings.trevorTheTrapper.trackedEntityProximityIndicator", Feature.TREVOR_THE_TRAPPER_FEATURES),
    TREVOR_HIGHLIGHT_TRACKED_ENTITY("settings.trevorTheTrapper.highlightTrackedEntity", Feature.TREVOR_THE_TRAPPER_FEATURES),
    TREVOR_SHOW_QUEST_COOLDOWN("settings.trevorTheTrapper.showQuestCooldown", Feature.TREVOR_THE_TRAPPER_FEATURES),
    SHOW_FETCHUR_ONLY_IN_DWARVENS("settings.showFetchurOnlyInDwarven", Feature.FETCHUR_TODAY),
    SHOW_FETCHUR_ITEM_NAME("settings.showFetchurItemName", Feature.FETCHUR_TODAY),
    SHOW_FETCHUR_INVENTORY_OPEN_ONLY("settings.showFetchurInventoryOpenOnly", Feature.FETCHUR_TODAY),
    WARN_WHEN_FETCHUR_CHANGES("settings.warnWhenFetchurChanges", Feature.FETCHUR_TODAY),
    STOP_ONLY_RAT_SQUEAK("settings.onlyStopRatSqueak", Feature.STOP_RAT_SOUNDS),
    ENDER_CHEST_PREVIEW("settings.showEnderChestPreview", Feature.SHOW_BACKPACK_PREVIEW),
    HIDE_WHEN_NOT_IN_END("settings.hideWhenNotDoingQuest", Feature.VOIDGLOOM_SLAYER_TRACKER),
    HEALTH_PREDICTION("settings.vanillaHealthPrediction", Feature.HEALTH_BAR),
    OUTLINE_DUNGEON_TEAMMATES("settings.outlineDungeonTeammates", Feature.ENTITY_OUTLINES),
    ITEM_GLOW("settings.glowingDroppedItems", Feature.ENTITY_OUTLINES),
    ABBREVIATE_SKILL_XP_DENOMINATOR("settings.abbreviateSkillXpDenominator", Feature.SKILL_DISPLAY),
    DISABLE_SPIRIT_SCEPTRE_MESSAGES("settings.disableDamageChatMessages", Feature.SPIRIT_SCEPTRE_DISPLAY),
    OUTBID_ALERT("settings.outbidAlertSound", Feature.OUTBID_ALERT_SOUND),
    DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME("settings.dontReplaceRomanNumeralsInItemNames", Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS),
    RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU("settings.resetSalvagedEssencesAfterLeavingMenu", Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY),
    CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD("settings.changeDungeonMapZoomWithKeyboard", Feature.DUNGEONS_MAP_DISPLAY),
    DUNGEON_MAP_ZOOM("settings.mapZoom", Feature.DUNGEONS_MAP_DISPLAY),
    SHOW_PROFILE_TYPE( "settings.showProfileType", Feature.PLAYER_SYMBOLS_IN_CHAT),
    SHOW_NETHER_FACTION("settings.showNetherFaction", Feature.PLAYER_SYMBOLS_IN_CHAT),
    HIDE_WHEN_NOT_IN_CRIMSON("settings.hideWhenNotDoingQuest", Feature.INFERNO_SLAYER_TRACKER),
    EXPAND_DEPLOYABLE_STATUS("settings.expandDeployableStatus", Feature.DEPLOYABLE_STATUS_DISPLAY),
    TREVOR_BETTER_NAMETAG("settings.trevorTheTrapper.betterNametag", Feature.TREVOR_THE_TRAPPER_FEATURES),
    HIDE_WHEN_NOT_IN_RIFT("settings.hideWhenNotDoingQuest", Feature.RIFTSTALKER_SLAYER_TRACKER),
    ABBREVIATE_DRILL_FUEL_DENOMINATOR("settings.abbreviateDrillFuelDenominator", Feature.DRILL_FUEL_TEXT),
    SHOW_ONLY_HOLDING_FISHING_ROD("settings.showOnlyHoldingFishingRod", Feature.DOLPHIN_PET_TRACKER),
    HIDE_HEALTH_BAR_ON_RIFT("settings.hideHealthThingsOnRift", Feature.HEALTH_BAR),
    HIDE_HEALTH_TEXT_ON_RIFT("settings.hideHealthThingsOnRift", Feature.HEALTH_TEXT),
    HIDE_HEALTH_UPDATES_ON_RIFT("settings.hideHealthThingsOnRift", Feature.HEALTH_UPDATES),
    HIDE_ONLY_OUTSIDE_RIFT("settings.hideOnlyOutsideRift", Feature.HIDE_HEALTH_BAR),
    FIRE_FREEZE_SOUND("settings.fireFreezeSound", Feature.FIRE_FREEZE_TIMER),
    FIRE_FREEZE_WHEN_HOLDING("settings.fireFreezeWhenHolding", Feature.FIRE_FREEZE_TIMER),
    HEART_INSTEAD_HEALTH_ON_RIFT("settings.heartInsteadHealthOnRift", Feature.HEALTH_TEXT),
    OUTLINE_SHOWCASE_ITEMS("settings.outlineShowcaseItems", Feature.ENTITY_OUTLINES),
    BUILDERS_TOOL_PREVIEW("settings.showBuildersToolPreview", Feature.SHOW_BACKPACK_PREVIEW),
    HEALTH_TEXT_ICON("settings.healthTextIcon", Feature.HEALTH_TEXT),
    MANA_TEXT_ICON("settings.manaTextIcon", Feature.MANA_TEXT),
    DEFENCE_TEXT_ICON("settings.defenceTextIcon", Feature.DEFENCE_TEXT),
    EFFECTIVE_HEALTH_TEXT_ICON("settings.effectiveHealthTextIcon", Feature.EFFECTIVE_HEALTH_TEXT),
    ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR("settings.abbreviateThunderDisplaysDenominator", Feature.THUNDER_BOTTLE_DISPLAY),
    CLASS_COLORED_TEAMMATE("settings.classColoredTeammate", Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY),
    DRAW_LOCK_ONLY_WHEN_HOVERED("settings.drawLockOnlyWhenHovered", Feature.LOCK_SLOTS),
    FARMING_TOOLS_PREVIEW("settings.showFarmingToolsPreview", Feature.SHOW_BACKPACK_PREVIEW),
    RENDER_ITEM_ON_LOG("settings.renderItemOnLog", Feature.ITEM_PICKUP_LOG),

    DISCORD_RP_DETAILS("messages.firstStatus", Feature.DISCORD_RPC),
    DISCORD_RP_STATE("messages.secondStatus", Feature.DISCORD_RPC),
    DISCORD_RP_CUSTOM_DETAILS(Feature.DISCORD_RPC),
    DISCORD_RP_CUSTOM_STATE(Feature.DISCORD_RPC),
    DISCORD_RP_AUTO_MODE(Feature.DISCORD_RPC),
    ;

    private final Feature relatedFeature;
    private final String translationKey;
    @Getter private final boolean universal;

    /** For Universal settings */
    @Getter @Setter private Feature universalFeature;

    /** Universal FeatureSetting */
    FeatureSetting(String translationKey) {
        this.translationKey = translationKey;
        this.relatedFeature = null;
        this.universal = true;
    }

    /** FeatureSetting with dynamic translation */
    FeatureSetting(Feature relatedFeature) {
        this.translationKey = null;
        this.relatedFeature = relatedFeature;
        this.universal = false;
    }

    /** Standard FeatureSetting */
    FeatureSetting(@NonNull String translationKey, @NonNull Feature relatedFeature) {
        this.translationKey = translationKey;
        this.relatedFeature = relatedFeature;
        this.universal = false;
    }

    public Feature getRelatedFeature() {
        for (Feature feature : Feature.values()) {
            if (feature == relatedFeature) {
                return feature;
            }
        }
        return null;
    }

    public String getMessage(String... variables) {
        if (translationKey != null) {
            return Translations.getMessage(translationKey, (Object[]) variables);
        } else {
            return null;
        }
    }
}