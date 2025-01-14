package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.FeatureSetting;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Getter
public enum Feature {

    DROP_CONFIRMATION(1, "settings.itemDropConfirmation", new GuiFeatureData(ColorCode.RED, true), true, FeatureSetting.ENABLED_IN_OTHER_GAMES),
    SHOW_BACKPACK_PREVIEW(3, "settings.showBackpackPreview", null, false, FeatureSetting.BACKPACK_STYLE, FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, FeatureSetting.MAKE_INVENTORY_COLORED, FeatureSetting.CAKE_BAG_PREVIEW, FeatureSetting.PERSONAL_COMPACTOR_PREVIEW, FeatureSetting.ENDER_CHEST_PREVIEW, FeatureSetting.BUILDERS_TOOL_PREVIEW),
    HIDE_BONES(4, "settings.hideSkeletonHatBones", null, false),
    SKELETON_BAR(5, "settings.skeletonHatBonesBar", new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR), false),
    HIDE_FOOD_ARMOR_BAR(6, "settings.hideFoodAndArmor", null, false),
    FULL_INVENTORY_WARNING(7, "settings.fullInventoryWarning", new GuiFeatureData(ColorCode.RED), false, FeatureSetting.REPEATING),
    SHOW_REFORGE_OVERLAY(10, "settings.showReforgeOverlay", null, false),
    MINION_STOP_WARNING(11, "settings.minionStopWarning", new GuiFeatureData(ColorCode.RED), true),
    HIDE_HEALTH_BAR(13, "settings.hideHealthBar", null, true, FeatureSetting.HIDE_ONLY_OUTSIDE_RIFT),
    DOUBLE_DROP_IN_OTHER_GAMES(14, null, false),
    MINION_FULL_WARNING(15, "settings.fullMinionWarning", new GuiFeatureData(ColorCode.RED), false),
    USE_VANILLA_TEXTURE_DEFENCE(17, "settings.useVanillaTexture", null, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, "settings.showOnlyWhenHoldingShift", null, true),
    MANA_BAR(19, "settings.manaBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.AQUA), false),
    MANA_TEXT(20, "settings.manaNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), false, FeatureSetting.MANA_TEXT_ICON),
    HEALTH_BAR(21, "settings.healthBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.RED), true, FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS, FeatureSetting.HEALTH_PREDICTION, FeatureSetting.HIDE_HEALTH_BAR_ON_RIFT),
    HEALTH_TEXT(22, "settings.healthNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), false, FeatureSetting.HIDE_HEALTH_TEXT_ON_RIFT, FeatureSetting.HEART_INSTEAD_HEALTH_ON_RIFT, FeatureSetting.HEALTH_TEXT_ICON),
    DEFENCE_ICON(23, "settings.defenseIcon", new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON), false, FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, "settings.defenseNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false, FeatureSetting.OTHER_DEFENCE_STATS, FeatureSetting.DEFENCE_TEXT_ICON),
    DEFENCE_PERCENTAGE(25, "settings.defensePercentage", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), true),
    HEALTH_UPDATES(26, "settings.healthUpdates", new GuiFeatureData(EnumUtils.DrawType.TEXT), false, FeatureSetting.HIDE_HEALTH_UPDATES_ON_RIFT), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, "settings.hidePlayersInLobby", null, true),
    DARK_AUCTION_TIMER(28, "settings.darkAuctionTimer", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false, FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, "settings.itemPickupLog", new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG), false),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, false),
    DONT_RESET_CURSOR_INVENTORY(37, "settings.dontResetCursorInventory", null, false),
    LOCK_SLOTS(38, "settings.lockSlots", null, false),
    SUMMONING_EYE_ALERT(39, "settings.summoningEyeAlert", new GuiFeatureData(ColorCode.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, "settings.makeEnderchestsInEndGreen", new GuiFeatureData(ColorCode.GREEN), false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, "settings.stopDroppingSellingRareItems", new GuiFeatureData(ColorCode.RED, true), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, "settings.makeBackpackInventoriesColored", null, false),
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, "settings.replaceRomanNumeralsWithNumbers", null, true, FeatureSetting.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, "settings.changeBarColorForPotions", null, false),
    FISHING_SOUND_INDICATOR(48, "settings.soundIndicatorForFishing", null, false),
    AVOID_BLINKING_NIGHT_VISION(49, "settings.avoidBlinkingNightVision", null, false),
    MINION_DISABLE_LOCATION_WARNING(50, "settings.disableMinionLocationWarning", null, false),
    ENCHANTMENT_LORE_PARSING(52, "settings.enchantmentLoreParsing", null, false, FeatureSetting.HIGHLIGHT_ENCHANTMENTS, FeatureSetting.PERFECT_ENCHANT_COLOR, FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR, FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR, FeatureSetting.ENCHANT_LAYOUT, FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS),
    SHOW_ITEM_COOLDOWNS(53, "settings.showItemCooldowns", null, false),
    SKILL_DISPLAY(54, "settings.collectionDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), false, FeatureSetting.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL, FeatureSetting.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP, FeatureSetting.SHOW_SKILL_XP_GAINED, FeatureSetting.ABBREVIATE_SKILL_XP_DENOMINATOR),
    SPEED_PERCENTAGE(55, "settings.speedPercentage", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.WHITE), false),
    SLAYER_ARMOR_PROGRESS(57, "settings.revenantIndicator", new GuiFeatureData(EnumUtils.DrawType.SLAYER_ARMOR_PROGRESS, ColorCode.AQUA), false),
    SPECIAL_ZEALOT_ALERT(58, "settings.specialZealotAlert", new GuiFeatureData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null, false),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null, false),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null, false),
    HIDE_PET_HEALTH_BAR(63, "settings.hidePetHealthBar", null, false),
    // Release v1.4
    DISABLE_MAGICAL_SOUP_MESSAGES(64, "settings.disableMagicalSoupMessage", null,true),
    DEPLOYABLE_STATUS_DISPLAY(65, "settings.deployableDisplay", new GuiFeatureData(EnumUtils.DrawType.DEPLOYABLE_DISPLAY, null), false, FeatureSetting.DEPLOYABLE_DISPLAY_STYLE, FeatureSetting.EXPAND_DEPLOYABLE_STATUS),
    ZEALOT_COUNTER(66, "settings.zealotCounter", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false, FeatureSetting.ZEALOT_SPAWN_AREAS_ONLY),
    TICKER_CHARGES_DISPLAY(67, "settings.tickerChargesDisplay", new GuiFeatureData(EnumUtils.DrawType.TICKER, null), false),
    NO_ARROWS_LEFT_ALERT(69, "settings.noArrowsLeftAlert", null, false),
    CAKE_BAG_PREVIEW(71, null, true),
    REPEAT_FULL_INVENTORY_WARNING(73, null, true),
    SBA_BUTTON_IN_PAUSE_MENU(76, "settings.skyblockAddonsButtonInPauseMenu", null, false),
    SHOW_TOTAL_ZEALOT_COUNT(77, "settings.showTotalZealotCount", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, FeatureSetting.ZEALOT_SPAWN_AREAS_ONLY),
    SHOW_SUMMONING_EYE_COUNT(78, "settings.showSummoningEyeCount", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, FeatureSetting.ZEALOT_SPAWN_AREAS_ONLY),
    SHOW_AVERAGE_ZEALOTS_PER_EYE(79, "settings.showZealotsPerEye", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, FeatureSetting.ZEALOT_SPAWN_AREAS_ONLY),
    TURN_BOW_COLOR_WHEN_USING_ARROW_POISON(80, "settings.turnBowGreenWhenUsingToxicArrowPoison", null, false),
    BIRCH_PARK_RAINMAKER_TIMER(81, "settings.birchParkRainmakerTimer", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false),
    DISCORD_RPC(83, "settings.discordRP", null, true, FeatureSetting.DISCORD_RP_DETAILS, FeatureSetting.DISCORD_RP_STATE),
    ENDSTONE_PROTECTOR_DISPLAY(84, "settings.endstoneProtectorDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.WHITE), false),
    FANCY_WARP_MENU(85, "settings.fancyWarpMenu", null, false),
    HIDE_GREY_ENCHANTS(87, "settings.hideGreyEnchants", null, false),
    LEGENDARY_SEA_CREATURE_WARNING(88, "settings.legendarySeaCreatureWarning", new GuiFeatureData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(90, null, false),
    BOSS_APPROACH_ALERT(91, "settings.bossApproachAlert", null, false, FeatureSetting.REPEATING),
    DISABLE_TELEPORT_PAD_MESSAGES(92, "settings.disableTeleportPadMessages", null, false),
    BAIT_LIST(93, "settings.baitListDisplay", new GuiFeatureData(EnumUtils.DrawType.BAIT_LIST_DISPLAY, ColorCode.AQUA), false),
    ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(94, "settings.zealotCounterExplosiveBow", null, true),
    DISABLE_ENDERMAN_TELEPORTATION_EFFECT(95, "settings.disableEndermanTeleportation", null, true),
    CHANGE_ZEALOT_COLOR(96, "settings.changeZealotColor", new GuiFeatureData(ColorCode.LIGHT_PURPLE), true),
    HIDE_SVEN_PUP_NAMETAGS(97, "settings.hideSvenPupNametags", null, true),
    REPEAT_SLAYER_BOSS_WARNING(98, null, true),
    // Release v1.5
    DUNGEONS_MAP_DISPLAY(99, "settings.dungeonMapDisplay", new GuiFeatureData(EnumUtils.DrawType.DUNGEONS_MAP, ColorCode.BLACK), false, FeatureSetting.ROTATE_MAP, FeatureSetting.CENTER_ROTATION_ON_PLAYER, FeatureSetting.SHOW_PLAYER_HEADS_ON_MAP, FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD, FeatureSetting.MAP_ZOOM),
    ROTATE_MAP(100, "settings.rotateMap", null, false),
    CENTER_ROTATION_ON_PLAYER(101, "settings.centerRotationOnYourPlayer", null, false),
    MAP_ZOOM(-1, "settings.mapZoom", null, false),
    MAKE_DROPPED_ITEMS_GLOW(102, "settings.glowingDroppedItems", null, false, FeatureSetting.SHOW_GLOWING_ITEMS_ON_ISLAND),
    OUTLINE_DUNGEON_TEAMMATES(103, null, null, false),
    SHOW_BASE_STAT_BOOST_PERCENTAGE(104, "settings.baseStatBoostPercentage", new GuiFeatureData(ColorCode.RED, true), false, FeatureSetting.COLOUR_BY_RARITY),
    BASE_STAT_BOOST_COLOR_BY_RARITY(105, "settings.colorByRarity", null, true),
    SHOW_PLAYER_HEADS_ON_MAP(106, "settings.showPlayerHeadsOnMap", null, true),
    SHOW_HEALING_CIRCLE_WALL(107, "settings.showHealingCircleWall", new GuiFeatureData(ColorCode.GREEN, false), true, FeatureSetting.HEALING_CIRCLE_OPACITY),
    SHOW_CRITICAL_DUNGEONS_TEAMMATES(108, "settings.showCriticalTeammates", null, false),
    SHOW_GLOWING_ITEMS_ON_ISLAND(109, "settings.showGlowingItemsOnIsland", null, false),
    SHOW_ITEM_DUNGEON_FLOOR(110, "settings.showItemDungeonFloor", new GuiFeatureData(ColorCode.RED, true), false),
    SHOW_DUNGEON_MILESTONE(111, "settings.showDungeonMilestone", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false),
    DUNGEONS_COLLECTED_ESSENCES_DISPLAY(112, "settings.dungeonsCollectedEssencesDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false, FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER, FeatureSetting.RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU),
    STOP_BONZO_STAFF_SOUNDS(113, "settings.stopBonzoStaffSounds", null, true),
    SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL(115, null, true),
    REVENANT_SLAYER_TRACKER(116, "settings.revenantSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_CRYPTS),
    TARANTULA_SLAYER_TRACKER(117, "settings.tarantulaSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_SPIDERS_DEN),
    SVEN_SLAYER_TRACKER(118, "settings.svenSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_CASTLE),
    REVENANT_COLOR_BY_RARITY(119, null, false),
    TARANTULA_COLOR_BY_RARITY(120, null, false),
    SVEN_COLOR_BY_RARITY(121, null, false),
    REVENANT_TEXT_MODE(122, null, true),
    TARANTULA_TEXT_MODE(123, null, true),
    SVEN_TEXT_MODE(124, null, true),
    DRAGON_STATS_TRACKER(125, "settings.dragonStatsTracker", new GuiFeatureData(EnumUtils.DrawType.DRAGON_STATS_TRACKER, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.DRAGONS_NEST_ONLY),
    DRAGON_STATS_TRACKER_COLOR_BY_RARITY(126, null, false),
    DRAGON_STATS_TRACKER_TEXT_MODE(127, null, false),
    DRAGON_STATS_TRACKER_NEST_ONLY(128, "settings.dragonsNestOnly", false),
    ZEALOT_COUNTER_ZEALOT_SPAWN_AREAS_ONLY(129, null, false),
    SHOW_TOTAL_ZEALOT_COUNT_ZEALOT_SPAWN_AREAS_ONLY(130, null, false),
    SHOW_SUMMONING_EYE_COUNT_ZEALOT_SPAWN_AREAS_ONLY(131, null, false),
    SHOW_AVERAGE_ZEALOTS_PER_EYE_ZEALOT_SPAWN_AREAS_ONLY(132, null, false),
    HIDE_WHEN_NOT_IN_CRYPTS(133, null, false),
    HIDE_WHEN_NOT_IN_SPIDERS_DEN(134, null, false),
    HIDE_WHEN_NOT_IN_CASTLE(135, null, false),
    DUNGEON_DEATH_COUNTER(136, "settings.dungeonDeathCounter", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), false),
    PERSONAL_COMPACTOR_PREVIEW(137, null, false),
    ROCK_PET_TRACKER(138, "settings.rockPetTracker", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), true),
    DOLPHIN_PET_TRACKER(139, "settings.dolphinPetTracker", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), true, FeatureSetting.SHOW_ONLY_HOLDING_FISHING_ROD),
    SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY(140, "settings.dungeonsTeammateNameOverlay", null, false, FeatureSetting.CLASS_COLORED_TEAMMATE),
    SHOW_STACKING_ENCHANT_PROGRESS(141, "settings.stackingEnchantProgress", new GuiFeatureData(ColorCode.RED, true), false),
    DUNGEONS_SECRETS_DISPLAY(142, "settings.dungeonsSecretsDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), false),
    SKILL_PROGRESS_BAR(143, "settings.skillProgressBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.GREEN), true),
    SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP(144, null, true),
    SHOW_SKILL_XP_GAINED(145, null, false),
    SHOW_SALVAGE_ESSENCES_COUNTER(146, null, false),
    DISABLE_MORT_MESSAGES(147, "settings.disableMortMessages", null, false),
    DISABLE_BOSS_MESSAGES(148, "settings.disableBossMessages", null, false),
    SHOW_SWORD_KILLS(149, "settings.showSwordKills", new GuiFeatureData(ColorCode.RED, true), false),
    HIDE_OTHER_PLAYERS_PRESENTS(150, "settings.hideOtherPlayersPresents", null, false),
    COMPACT_TAB_LIST(152, "settings.compactTabList", null, false),
    ENCHANTMENTS_HIGHLIGHT(153, "settings.highlightSpecialEnchantments", null, false),
    CANDY_POINTS_COUNTER(155, "settings.candyPointsCounter", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false),
    HEALING_CIRCLE_OPACITY(156, "settings.healingCircleOpacity", null, false),
    SHOW_EXPERIMENTATION_TABLE_TOOLTIPS(158, "settings.showExperimentationTableTooltips", null, true),
    DRILL_FUEL_BAR(160, "settings.drillFuelBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.DARK_GREEN), false),
    DRILL_FUEL_TEXT(161, "settings.drillFuelNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_GREEN), false, FeatureSetting.ABBREVIATE_DRILL_FUEL_DENOMINATOR),
    FISHING_PARTICLE_OVERLAY(162, "settings.fishingParticleOverlay", new GuiFeatureData(ColorCode.WHITE), false, FeatureSetting.BIGGER_WAKE),
    ENCHANTMENT_PERFECT_COLOR(165, "enchants.superTier", new GuiFeatureData(ColorCode.CHROMA, true), false),
    ENCHANTMENT_GREAT_COLOR(166, "enchants.highTier", new GuiFeatureData(ColorCode.GOLD, true), false),
    ENCHANTMENT_GOOD_COLOR(167, "enchants.midTier", new GuiFeatureData(ColorCode.BLUE, true), false),
    ENCHANTMENT_POOR_COLOR(168, "enchants.lowTier", new GuiFeatureData(ColorCode.GRAY, true), false),
    BIGGER_WAKE(170, "settings.biggerWake", null, false),
    ENCHANTMENT_COMMA_COLOR(171, "enchants.commas", new GuiFeatureData(ColorCode.BLUE, true), false),
    REFORGE_FILTER(172, "settings.reforgeFilter", null, false),
    TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR(173, "settings.trevorTheTrapper.trackedEntityProximityIndicator", new GuiFeatureData(EnumUtils.DrawType.PROXIMITY_INDICATOR, null), false),
    TREVOR_HIGHLIGHT_TRACKED_ENTITY(174, "settings.trevorTheTrapper.highlightTrackedEntity", null, false),
    TREVOR_SHOW_QUEST_COOLDOWN(175, "settings.trevorTheTrapper.showQuestCooldown", null, false),
    HIDE_ENCHANT_DESCRIPTION(176, "settings.hideEnchantDescription", null, true),
    TREVOR_THE_TRAPPER_FEATURES(177, "settings.trevorTheTrapper.title", null, false, FeatureSetting.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY, FeatureSetting.TREVOR_BETTER_NAMETAG, FeatureSetting.TREVOR_SHOW_QUEST_COOLDOWN),
    FETCHUR_TODAY(178, "settings.fetchurToday", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false, FeatureSetting.SHOW_FETCHUR_ITEM_NAME, FeatureSetting.SHOW_FETCHUR_ONLY_IN_DWARVENS, FeatureSetting.SHOW_FETCHUR_INVENTORY_OPEN_ONLY, FeatureSetting.WARN_WHEN_FETCHUR_CHANGES),
    SHOW_FETCHUR_ONLY_IN_DWARVENS(179, "settings.showFetchurOnlyInDwarven", null, true),
    SHOW_FETCHUR_ITEM_NAME(180, "settings.showFetchurItemName", null, true),
    SHOW_FETCHUR_INVENTORY_OPEN_ONLY(181, "settings.showFetchurInventoryOpenOnly", null, true),
    WARN_WHEN_FETCHUR_CHANGES(182, "settings.warnWhenFetchurChanges", new GuiFeatureData(ColorCode.RED), true),
    STOP_RAT_SOUNDS(183, "settings.stopRatSounds", null, true, FeatureSetting.STOP_ONLY_RAT_SQUEAK),
    STOP_ONLY_RAT_SQUEAK(184, "settings.onlyStopRatSqueak", null, true),
    SHOW_ENDER_CHEST_PREVIEW(185, "settings.showEnderChestPreview", null, false),
    VOIDGLOOM_SLAYER_TRACKER(186, "settings.voidgloomSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_END),
    HIDE_WHEN_NOT_IN_END(187, null, false),
    VOIDGLOOM_COLOR_BY_RARITY(188, null, false),
    VOIDGLOOM_TEXT_MODE(189, null, true),
    HIDE_PLAYERS_NEAR_NPCS(190, "settings.hidePlayersNearNPCs", null, false),
    OVERFLOW_MANA(191, "settings.showOverflowManaNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false),
    DOUBLE_WARP(192, "settings.doubleWarp", null, true),
    //JUNGLE_AXE_COOLDOWN(193, "settings.axeCooldownIndicator", null, true, EnumUtils.FeatureSetting.COOLDOWN_PREDICTION, EnumUtils.FeatureSetting.LEVEL_100_LEG_MONKEY),
    HEALTH_PREDICTION(194, "settings.vanillaHealthPrediction", null, true),
    DISABLE_EMPTY_GLASS_PANES(195, "settings.hideMenuGlassPanes", null, false),
    ENTITY_OUTLINES(196, "settings.entityOutlines", null, false, FeatureSetting.OUTLINE_DUNGEON_TEAMMATES, FeatureSetting.ITEM_GLOW, FeatureSetting.OUTLINE_SHOWCASE_ITEMS, FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY),
    EFFECTIVE_HEALTH_TEXT(197, "settings.effectiveHealthNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_GREEN), false, FeatureSetting.EFFECTIVE_HEALTH_TEXT_ICON),
    ABBREVIATE_SKILL_XP_DENOMINATOR(198, "settings.abbreviateSkillXpDenominator", null, true),
    OTHER_DEFENCE_STATS(199, "settings.otherDefenseStats", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false),
    // Release v1.6
    PREVENT_MOVEMENT_ON_DEATH(200, "settings.preventMovementOnDeath", null, true),
    HIDE_SPAWN_POINT_PLAYERS(201, "settings.hideSpawnPointPlayers", null, true),
    SPIRIT_SCEPTRE_DISPLAY(202, "settings.showSpiritSceptreDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), false, FeatureSetting.DISABLE_SPIRIT_SCEPTRE_MESSAGES),
    DISABLE_SPIRIT_SCEPTRE_MESSAGES(203, null, false),
    FARM_EVENT_TIMER(204, "settings.jacobsContestTimer", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false, FeatureSetting.ENABLED_IN_OTHER_GAMES),
    SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES(205, null, false),
    OUTBID_ALERT_SOUND(206, "settings.outbidAlertSound", null, true, FeatureSetting.ENABLED_IN_OTHER_GAMES),
    BROOD_MOTHER_ALERT(207, "settings.broodMotherWarning", null, false),
    BAL_BOSS_ALERT(208, "settings.balBossWarning", null, false),
    OUTBID_ALERT_SOUND_IN_OTHER_GAMES(209, null, false),
    DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME(210, "settings.dontReplaceRomanNumeralsInItemNames", null, true),
    BACKPACK_OPENING_SOUND(211, "settings.backpackOpeningSound", null, false),
    DEVELOPER_MODE(212, "settings.devMode", null, true),
    SHOW_SKYBLOCK_ITEM_ID(213, "settings.showSkyblockItemId", null, true),
    RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU(214, "settings.resetSalvagedEssencesAfterLeavingMenu", null, false),
    CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD(215, null, false),
    // Release 1.7
    PLAYER_SYMBOLS_IN_CHAT(216, "settings.showPlayerSymbolsInChat", null, false, FeatureSetting.SHOW_PROFILE_TYPE, FeatureSetting.SHOW_NETHER_FACTION),
    CRIMSON_ARMOR_ABILITY_STACKS(217, "settings.crimsonArmorAbilityStacks", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false),
    HIDE_TRUE_DEFENSE(218, "settings.hideTrueDefense", new GuiFeatureData(ColorCode.RED), false),
    SHOW_PROFILE_TYPE(219, null, false),
    SHOW_NETHER_FACTION(220,null, false),
    // Release Fix3dll
	HIDE_WHEN_NOT_IN_CRIMSON(222, null, false),
    INFERNO_SLAYER_TRACKER(223, "settings.infernoSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_CRIMSON),
    INFERNO_COLOR_BY_RARITY(224, null, false),
    INFERNO_TEXT_MODE(225, null, true),
    EXPAND_DEPLOYABLE_STATUS(226, null, true),
    TREVOR_BETTER_NAMETAG(227, null, false),
    RIFTSTALKER_SLAYER_TRACKER(228, "settings.riftstalkerSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, FeatureSetting.COLOUR_BY_RARITY, FeatureSetting.TEXT_MODE, FeatureSetting.HIDE_WHEN_NOT_IN_RIFT),
    RIFTSTALKER_COLOR_BY_RARITY(229, null, false),
    RIFTSTALKER_TEXT_MODE(230, null, true),
    HIDE_WHEN_NOT_IN_RIFT(231, null, false),
    ABBREVIATE_DRILL_FUEL_DENOMINATOR(232, null, true),
    SHOW_ONLY_HOLDING_FISHING_ROD(233, null, null, true),
    HIDE_HEALTH_BAR_ON_RIFT(234, null, true),
    HIDE_HEALTH_TEXT_ON_RIFT(235, null, true),
    HIDE_HEALTH_UPDATES_ON_RIFT(236, null, true),
    HIDE_ONLY_OUTSIDE_RIFT(237, null, true),
    FIRE_FREEZE_TIMER(238, "settings.fireFreezeTimer", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false, FeatureSetting.FIRE_FREEZE_SOUND, FeatureSetting.FIRE_FREEZE_WHEN_HOLDING),
    FIRE_FREEZE_SOUND(239, null, false),
    FIRE_FREEZE_WHEN_HOLDING(240, null, false),
    HIDE_HAUNTED_SKULLS(241, "settings.hideHauntedSkulls", null, true),
    THUNDER_BOTTLE_DISPLAY(242, "settings.thunderBottleDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_PURPLE), false, FeatureSetting.ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR),
    HEART_INSTEAD_HEALTH_ON_RIFT(244, null, true),
    OUTLINE_SHOWCASE_ITEMS(245, null, false),
    PET_DISPLAY(246, "settings.petDisplay", new GuiFeatureData(EnumUtils.DrawType.PET_DISPLAY, ColorCode.GOLD), false, FeatureSetting.PET_ITEM_STYLE),
    BUILDERS_TOOL_PREVIEW(248, null, false),
    HEALTH_TEXT_ICON(249, "settings.healthTextIcon", true),
    MANA_TEXT_ICON(250, "settings.manaTextIcon", true),
    DEFENCE_TEXT_ICON(251, "settings.defenceTextIcon", true),
    EFFECTIVE_HEALTH_TEXT_ICON(252, "settings.effectiveHealthTextIcon", true),
    ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR(253, null, true),
    CLASS_COLORED_TEAMMATE(258, null, false),


    WARNING_TIME(-1, "settings.warningDuration", false),
    LANGUAGE(-1, "language", false),
    EDIT_LOCATIONS(-1, "settings.editLocations", false),
    RESET_LOCATION(-1, "settings.resetLocations",false),
    RESCALE_FEATURES(-1, "messages.rescaleFeatures", false),
    GENERAL_SETTINGS(-1, "settings.tab.generalSettings", false),
    TEXT_STYLE(-1, "settings.textStyle", false),
    CHROMA_SPEED(-1, "settings.chromaSpeed", false),
    CHROMA_MODE(-1, "settings.chromaMode", false),
    CHROMA_SIZE(-1, "settings.chromaSize", false),
    CHROMA_SATURATION(-1, "settings.chromaSaturation", false),
    CHROMA_BRIGHTNESS(-1, "settings.chromaBrightness",  false),
    TURN_ALL_FEATURES_CHROMA(-1, "settings.turnAllFeaturesChroma",  false),
    NUMBER_SEPARATORS(221, "settings.numberSeparators", false),
    TURN_ALL_TEXTS_CHROMA(243, "settings.turnAllTextsChroma", true),
    ENABLE_FEATURE_SNAPPING(254, "messages.enableFeatureSnapping", false),
    SHOW_COLOR_ICONS(256, "messages.showColorIcons", false),
    X_ALLIGNMENT(257, "settings.xAllignment", true),
    ;


    /**
     * These are "features" that are not actually features, but just hold the place of a setting. If you are adding any new settings and create
     * a feature here, make sure to add it!
     */
    private static final Set<Feature> SETTINGS = Sets.newHashSet(DOUBLE_DROP_IN_OTHER_GAMES, SHOW_PROFILE_TYPE,
            USE_VANILLA_TEXTURE_DEFENCE, SHOW_BACKPACK_HOLDING_SHIFT, OUTLINE_DUNGEON_TEAMMATES, SHOW_NETHER_FACTION,
            MAKE_BACKPACK_INVENTORIES_COLORED, CHANGE_BAR_COLOR_FOR_POTIONS, ENABLE_MESSAGE_WHEN_BREAKING_STEMS,
            ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS, ENABLE_MESSAGE_WHEN_MINING_NETHER, CAKE_BAG_PREVIEW,
            REPEAT_FULL_INVENTORY_WARNING, DOUBLE_WARP, RIFTSTALKER_COLOR_BY_RARITY, RIFTSTALKER_TEXT_MODE,
            REPEAT_SLAYER_BOSS_WARNING, ROTATE_MAP, CENTER_ROTATION_ON_PLAYER, MAP_ZOOM, BASE_STAT_BOOST_COLOR_BY_RARITY,
            SHOW_PLAYER_HEADS_ON_MAP, SHOW_GLOWING_ITEMS_ON_ISLAND, SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL, REVENANT_COLOR_BY_RARITY,
            TARANTULA_COLOR_BY_RARITY, SVEN_COLOR_BY_RARITY, REVENANT_TEXT_MODE, TARANTULA_TEXT_MODE, SVEN_TEXT_MODE,
            DRAGON_STATS_TRACKER_COLOR_BY_RARITY, HIDE_WHEN_NOT_IN_CASTLE, HIDE_WHEN_NOT_IN_SPIDERS_DEN, HIDE_WHEN_NOT_IN_END,
            VOIDGLOOM_COLOR_BY_RARITY, VOIDGLOOM_TEXT_MODE, HIDE_WHEN_NOT_IN_CRIMSON, INFERNO_COLOR_BY_RARITY, INFERNO_TEXT_MODE,
            HIDE_WHEN_NOT_IN_CRYPTS, HIDE_WHEN_NOT_IN_RIFT, PERSONAL_COMPACTOR_PREVIEW, SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP,
            SHOW_SKILL_XP_GAINED, SHOW_SALVAGE_ESSENCES_COUNTER, HEALING_CIRCLE_OPACITY, ENCHANTMENTS_HIGHLIGHT,
            ENCHANTMENT_COMMA_COLOR, ENCHANTMENT_PERFECT_COLOR, ENCHANTMENT_GREAT_COLOR, ENCHANTMENT_GOOD_COLOR,
            ENCHANTMENT_POOR_COLOR, BIGGER_WAKE, HIDE_ENCHANT_DESCRIPTION, HIDE_GREY_ENCHANTS,
            TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, TREVOR_HIGHLIGHT_TRACKED_ENTITY, TREVOR_SHOW_QUEST_COOLDOWN,
            SHOW_FETCHUR_ONLY_IN_DWARVENS, SHOW_FETCHUR_ITEM_NAME, SHOW_FETCHUR_INVENTORY_OPEN_ONLY, WARN_WHEN_FETCHUR_CHANGES,
            STOP_ONLY_RAT_SQUEAK, SHOW_ENDER_CHEST_PREVIEW, HEALTH_PREDICTION, ABBREVIATE_SKILL_XP_DENOMINATOR, OTHER_DEFENCE_STATS,
            DISABLE_SPIRIT_SCEPTRE_MESSAGES, OUTBID_ALERT_SOUND_IN_OTHER_GAMES, DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME,
            RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU, ABBREVIATE_DRILL_FUEL_DENOMINATOR, TREVOR_BETTER_NAMETAG,
            SHOW_ONLY_HOLDING_FISHING_ROD, HIDE_HEALTH_BAR_ON_RIFT, HIDE_HEALTH_TEXT_ON_RIFT, HIDE_HEALTH_UPDATES_ON_RIFT,
            HIDE_ONLY_OUTSIDE_RIFT, FIRE_FREEZE_SOUND, FIRE_FREEZE_WHEN_HOLDING, HEART_INSTEAD_HEALTH_ON_RIFT,
            OUTLINE_SHOWCASE_ITEMS, CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD, DRAGON_STATS_TRACKER_NEST_ONLY,
            HEALTH_TEXT_ICON, MANA_TEXT_ICON, DEFENCE_TEXT_ICON, EFFECTIVE_HEALTH_TEXT_ICON);

    /**
     * Features that are considered gui ones. This is used for examnple when saving the config to ensure that these features'
     * coordinates and colors are handled properly.
     */
    @Getter
    private static final Set<Feature> guiFeatures = new LinkedHashSet<>(Arrays.asList(DRILL_FUEL_BAR, SKILL_PROGRESS_BAR, MANA_BAR, HEALTH_BAR,
            MANA_TEXT, OVERFLOW_MANA, DEFENCE_ICON, DEFENCE_TEXT, EFFECTIVE_HEALTH_TEXT, PET_DISPLAY,
            DEFENCE_PERCENTAGE, HEALTH_TEXT, SKELETON_BAR, HEALTH_UPDATES, ITEM_PICKUP_LOG, DARK_AUCTION_TIMER, SKILL_DISPLAY, SPEED_PERCENTAGE,
            SLAYER_ARMOR_PROGRESS, DEPLOYABLE_STATUS_DISPLAY, ZEALOT_COUNTER, TICKER_CHARGES_DISPLAY, SHOW_TOTAL_ZEALOT_COUNT, SHOW_SUMMONING_EYE_COUNT,
            SHOW_AVERAGE_ZEALOTS_PER_EYE, BIRCH_PARK_RAINMAKER_TIMER, ENDSTONE_PROTECTOR_DISPLAY, BAIT_LIST, DUNGEONS_MAP_DISPLAY, SHOW_DUNGEON_MILESTONE,
            DUNGEONS_COLLECTED_ESSENCES_DISPLAY, REVENANT_SLAYER_TRACKER, TARANTULA_SLAYER_TRACKER, SVEN_SLAYER_TRACKER, DRAGON_STATS_TRACKER, DUNGEON_DEATH_COUNTER,
            ROCK_PET_TRACKER, DOLPHIN_PET_TRACKER, DUNGEONS_SECRETS_DISPLAY, CANDY_POINTS_COUNTER, DRILL_FUEL_TEXT, INFERNO_SLAYER_TRACKER,
            TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, FETCHUR_TODAY, VOIDGLOOM_SLAYER_TRACKER, OTHER_DEFENCE_STATS, SPIRIT_SCEPTRE_DISPLAY, FARM_EVENT_TIMER,
            CRIMSON_ARMOR_ABILITY_STACKS, HIDE_TRUE_DEFENSE, RIFTSTALKER_SLAYER_TRACKER, FIRE_FREEZE_TIMER, THUNDER_BOTTLE_DISPLAY));

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter
    private static final Set<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE, WARNING_TIME,
            CHROMA_SPEED, CHROMA_MODE, CHROMA_SIZE, TURN_ALL_FEATURES_CHROMA, CHROMA_SATURATION, CHROMA_BRIGHTNESS,
            NUMBER_SEPARATORS, DEVELOPER_MODE, TURN_ALL_TEXTS_CHROMA, SBA_BUTTON_IN_PAUSE_MENU, X_ALLIGNMENT));

    @Getter
    private static final Set<Feature> editGuiFeatures = new LinkedHashSet<>(Arrays.asList(
            RESET_LOCATION, RESCALE_FEATURES, SHOW_COLOR_ICONS, ENABLE_FEATURE_SNAPPING, X_ALLIGNMENT
    ));

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final int ID_AT_PREVIOUS_UPDATE = 221;

    private final int id;
    private final List<FeatureSetting> settings;
    private final GuiFeatureData guiFeatureData;
    private final boolean defaultDisabled;
    private final String translationKey;

    Feature(int id, String translationKey, boolean defaultDisabled) {
        this(id, translationKey, null, defaultDisabled);
    }

    Feature(int id, String translationKey, GuiFeatureData guiFeatureData, boolean defaultDisabled, FeatureSetting... settings) {
        this.id = id;
        this.translationKey = translationKey;
        this.settings = new ArrayList<>(Arrays.asList(settings));
        this.guiFeatureData = guiFeatureData;
        this.defaultDisabled = defaultDisabled;

        Set<Integer> registeredFeatureIDs = SkyblockAddons.getInstance().getRegisteredFeatureIDs();
        if (id != -1 && registeredFeatureIDs.contains(id)) {
            throw new RuntimeException("Multiple features have the same IDs!");
        } else {
            registeredFeatureIDs.add(id);
        }
    }

    /**
     * Called right after a feature's enable state is changed.
     */
    public void onToggle() {
        if (this.id == DEVELOPER_MODE.id) {
            if (DEVELOPER_MODE.isEnabled()) {
                SkyblockKeyBinding.DEVELOPER_COPY_NBT.register();
            } else if (SkyblockKeyBinding.DEVELOPER_COPY_NBT.isRegistered()) {
                SkyblockKeyBinding.DEVELOPER_COPY_NBT.deRegister();
            }
        }
    }

    /**
     * Sets whether the current feature is enabled.
     *
     * @param enabled {@code true} to enable the feature, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (enabled) {
            main.getConfigValues().getDisabledFeatures().remove(this);
        } else {
            main.getConfigValues().getDisabledFeatures().add(this);
        }
        onToggle();
        main.getConfigValues().saveConfig();
    }

    public boolean isActualFeature() {
        return id != -1 && getMessage() != null && !SETTINGS.contains(this);
    }

    public String getMessage(String... variables) {
        if (translationKey != null) {
            return Translations.getMessage(translationKey, (Object[]) variables);
        }

        return null;
    }

    public static Feature fromId(int id) {
        for (Feature feature : values()) {
            if (feature.getId() == id) {
                return feature;
            }
        }
        return null;
    }

    public boolean isGuiFeature() {
        return guiFeatures.contains(this);
    }

    public boolean isColorFeature() {
        return guiFeatureData != null && guiFeatureData.getDefaultColor() != null;
    }

    public ColorCode getDefaultColor() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultColor();
        }
        return null;
    }

    public boolean isNew() {
        return id > ID_AT_PREVIOUS_UPDATE;
    }

    public boolean isEnabled() {
        return !isDisabled();
    }

    public boolean isDisabled() {
        ConfigValues values = SkyblockAddons.getInstance().getConfigValues();
        return values != null && (values.getDisabledFeatures().contains(this) || this.isRemoteDisabled());
    }

    /**
     * Checks that all specified {@link Feature}s are active
     * @param features {@link Feature}(s)
     * @return true if they are enabled else false
     */
    public static boolean areEnabled(Feature... features) {
        if (features == null || features.length == 0) {
            throw new IllegalArgumentException("\"features\" cannot be null or empty");
        }

        for (Feature feature : features) {
            if (feature.isDisabled()) {
                return false; // short-circuit
            }
        }
        return true;
    }

    /**
     * Checks the received {@code OnlineData} to determine if the given feature should be disabled.This method checks
     * the list of features to be disabled for all versions first and then checks the list of features that should be
     * disabled for this specific version.
     * @return {@code true} if the feature should be disabled, {@code false} otherwise
     */
    public boolean isRemoteDisabled() {
        HashMap<String, List<Integer>> disabledFeatures = SkyblockAddons.getInstance().getOnlineData().getDisabledFeatures();

        if (disabledFeatures.containsKey("all")) {
            if (disabledFeatures.get("all") != null) {
                if (disabledFeatures.get("all").contains(this.getId())) {
                    return true;
                }
            } else {
                LOGGER.error("\"all\" key in disabled features map has value of null. Please fix online data.");
            }
        }

        /*
        Check for disabled features for this mod version. Pre-release versions will follow the disabled features
        list for their release version. For example, the version {@code 1.6.0-beta.10} will adhere to the list
        for version {@code 1.6.0}
         */
        String version = SkyblockAddons.VERSION;
        if (version.contains("-")) {
            version = version.split("-")[0];
        }
        if (disabledFeatures.containsKey(version)) {
            if (disabledFeatures.get(version) != null) {
                return disabledFeatures.get(version).contains(this.getId());
            } else {
                LOGGER.error("\"{}\" key in disabled features map has value of null. Please fix online data.", version);
            }
        }

        return false;
    }

    public boolean isInChromaFeatures() {
        return SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(this);
    }

}