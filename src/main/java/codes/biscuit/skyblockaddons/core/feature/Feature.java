package codes.biscuit.skyblockaddons.core.feature;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AnchorPoint;
import codes.biscuit.skyblockaddons.utils.EnumUtils.DrawType;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

@Getter
public enum Feature {
    DROP_CONFIRMATION(1, "settings.itemDropConfirmation", new FeatureGuiData(ColorCode.RED, true), true),
    SHOW_BACKPACK_PREVIEW(3, "settings.showBackpackPreview", null, false),
    HIDE_BONES(4, "settings.hideSkeletonHatBones", null, false),
    SKELETON_BAR(5, "settings.skeletonHatBonesBar", new FeatureGuiData(DrawType.SKELETON_BAR), false),
    HIDE_FOOD_ARMOR_BAR(6, "settings.hideFoodAndArmor", null, false),
    FULL_INVENTORY_WARNING(7, "settings.fullInventoryWarning", new FeatureGuiData(ColorCode.RED), false),
    SHOW_REFORGE_OVERLAY(10, "settings.showReforgeOverlay", null, false),
    MINION_STOP_WARNING(11, "settings.minionStopWarning", new FeatureGuiData(ColorCode.RED), true),
    HIDE_HEALTH_BAR(13, "settings.hideHealthBar", null, true),
    MINION_FULL_WARNING(15, "settings.fullMinionWarning", new FeatureGuiData(ColorCode.RED), false),
    MANA_BAR(19, "settings.manaBar", new FeatureGuiData(DrawType.BAR, ColorCode.AQUA), false),
    MANA_TEXT(20, "settings.manaNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA), false),
    HEALTH_BAR(21, "settings.healthBar", new FeatureGuiData(DrawType.BAR, ColorCode.RED), true),
    HEALTH_TEXT(22, "settings.healthNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.RED), false),
    DEFENCE_ICON(23, "settings.defenseIcon", new FeatureGuiData(DrawType.DEFENCE_ICON), false),
    DEFENCE_TEXT(24, "settings.defenseNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN), false),
    DEFENCE_PERCENTAGE(25, "settings.defensePercentage", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN), true),
    HEALTH_UPDATES(26, "settings.healthUpdates", new FeatureGuiData(DrawType.TEXT), false),
    HIDE_PLAYERS_IN_LOBBY(27, "settings.hidePlayersInLobby", null, true),
    DARK_AUCTION_TIMER(28, "settings.darkAuctionTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD), false),
    ITEM_PICKUP_LOG(29, "settings.itemPickupLog", new FeatureGuiData(DrawType.PICKUP_LOG), false),
    DONT_RESET_CURSOR_INVENTORY(37, "settings.dontResetCursorInventory", null, false),
    LOCK_SLOTS(38, "settings.lockSlots", null, false),
    SUMMONING_EYE_ALERT(39, "settings.summoningEyeAlert", new FeatureGuiData(ColorCode.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, "settings.makeEnderchestsInEndGreen", new FeatureGuiData(ColorCode.GREEN), false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, "settings.stopDroppingSellingRareItems", new FeatureGuiData(ColorCode.RED, true), false),
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, "settings.replaceRomanNumeralsWithNumbers", null, true),
    FISHING_SOUND_INDICATOR(48, "settings.soundIndicatorForFishing", null, false),
    AVOID_BLINKING_NIGHT_VISION(49, "settings.avoidBlinkingNightVision", null, false),
    MINION_DISABLE_LOCATION_WARNING(50, "settings.disableMinionLocationWarning", null, false),
    ENCHANTMENT_LORE_PARSING(52, "settings.enchantmentLoreParsing", null, false),
    SHOW_ITEM_COOLDOWNS(53, "settings.showItemCooldowns", null, false),
    SKILL_DISPLAY(54, "settings.collectionDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA), false),
    SPEED_PERCENTAGE(55, "settings.speedPercentage", new FeatureGuiData(DrawType.TEXT, ColorCode.WHITE), false),
    SLAYER_ARMOR_PROGRESS(57, "settings.revenantIndicator", new FeatureGuiData(DrawType.SLAYER_ARMOR_PROGRESS, ColorCode.AQUA), false),
    SPECIAL_ZEALOT_ALERT(58, "settings.specialZealotAlert", new FeatureGuiData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null, false),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null, false),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null, false),
    HIDE_PET_HEALTH_BAR(63, "settings.hidePetHealthBar", null, false),
    // Release v1.4
    DISABLE_MAGICAL_SOUP_MESSAGES(64, "settings.disableMagicalSoupMessage", null,true),
    DEPLOYABLE_STATUS_DISPLAY(65, "settings.deployableDisplay", new FeatureGuiData(DrawType.DEPLOYABLE_DISPLAY, null), false),
    ZEALOT_COUNTER(66, "settings.zealotCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), false),
    TICKER_CHARGES_DISPLAY(67, "settings.tickerChargesDisplay", new FeatureGuiData(DrawType.TICKER, null), false),
    NO_ARROWS_LEFT_ALERT(69, "settings.noArrowsLeftAlert", null, false),
    SBA_BUTTON_IN_PAUSE_MENU(76, "settings.skyblockAddonsButtonInPauseMenu", null, false),
    SHOW_TOTAL_ZEALOT_COUNT(77, "settings.showTotalZealotCount", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), true),
    SHOW_SUMMONING_EYE_COUNT(78, "settings.showSummoningEyeCount", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), true),
    SHOW_AVERAGE_ZEALOTS_PER_EYE(79, "settings.showZealotsPerEye", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), true),
    TURN_BOW_COLOR_WHEN_USING_ARROW_POISON(80, "settings.turnBowGreenWhenUsingToxicArrowPoison", null, false),
    BIRCH_PARK_RAINMAKER_TIMER(81, "settings.birchParkRainmakerTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), false),
    DISCORD_RPC(83, "settings.discordRP", null, true),
    ENDSTONE_PROTECTOR_DISPLAY(84, "settings.endstoneProtectorDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.WHITE), false),
    FANCY_WARP_MENU(85, "settings.fancyWarpMenu", null, false),
    LEGENDARY_SEA_CREATURE_WARNING(88, "settings.legendarySeaCreatureWarning", new FeatureGuiData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(90, null, false),
    BOSS_APPROACH_ALERT(91, "settings.bossApproachAlert", null, false),
    DISABLE_TELEPORT_PAD_MESSAGES(92, "settings.disableTeleportPadMessages", null, false),
    BAIT_LIST(93, "settings.baitListDisplay", new FeatureGuiData(DrawType.BAIT_LIST_DISPLAY, ColorCode.AQUA), false),
    ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(94, "settings.zealotCounterExplosiveBow", null, true),
    DISABLE_ENDERMAN_TELEPORTATION_EFFECT(95, "settings.disableEndermanTeleportation", null, true),
    CHANGE_ZEALOT_COLOR(96, "settings.changeZealotColor", new FeatureGuiData(ColorCode.LIGHT_PURPLE), true),
    HIDE_SVEN_PUP_NAMETAGS(97, "settings.hideSvenPupNametags", null, true),
    // Release v1.5
    DUNGEONS_MAP_DISPLAY(99, "settings.dungeonMapDisplay", new FeatureGuiData(DrawType.DUNGEONS_MAP, ColorCode.BLACK), false),
    MAKE_DROPPED_ITEMS_GLOW(102, "settings.glowingDroppedItems", null, false),
    SHOW_BASE_STAT_BOOST_PERCENTAGE(104, "settings.baseStatBoostPercentage", new FeatureGuiData(ColorCode.RED, true), false),
    SHOW_HEALING_CIRCLE_WALL(107, "settings.showHealingCircleWall", new FeatureGuiData(ColorCode.GREEN, false), true),
    SHOW_CRITICAL_DUNGEONS_TEAMMATES(108, "settings.showCriticalTeammates", null, false),
    SHOW_ITEM_DUNGEON_FLOOR(110, "settings.showItemDungeonFloor", new FeatureGuiData(ColorCode.RED, true), false),
    SHOW_DUNGEON_MILESTONE(111, "settings.showDungeonMilestone", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW), false),
    DUNGEONS_COLLECTED_ESSENCES_DISPLAY(112, "settings.dungeonsCollectedEssencesDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW), false),
    STOP_BONZO_STAFF_SOUNDS(113, "settings.stopBonzoStaffSounds", null, true),
    REVENANT_SLAYER_TRACKER(116, "settings.revenantSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    TARANTULA_SLAYER_TRACKER(117, "settings.tarantulaSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    SVEN_SLAYER_TRACKER(118, "settings.svenSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    DRAGON_STATS_TRACKER(125, "settings.dragonStatsTracker", new FeatureGuiData(DrawType.DRAGON_STATS_TRACKER, ColorCode.WHITE), false),
    DUNGEON_DEATH_COUNTER(136, "settings.dungeonDeathCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.RED), false),
    ROCK_PET_TRACKER(138, "settings.rockPetTracker", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY), true),
    DOLPHIN_PET_TRACKER(139, "settings.dolphinPetTracker", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA), true),
    SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY(140, "settings.dungeonsTeammateNameOverlay", null, false),
    SHOW_STACKING_ENCHANT_PROGRESS(141, "settings.stackingEnchantProgress", new FeatureGuiData(ColorCode.RED, true), false),
    DUNGEONS_SECRETS_DISPLAY(142, "settings.dungeonsSecretsDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY), false),
    SKILL_PROGRESS_BAR(143, "settings.skillProgressBar", new FeatureGuiData(DrawType.BAR, ColorCode.GREEN), true),
    DISABLE_MORT_MESSAGES(147, "settings.disableMortMessages", null, false),
    DISABLE_BOSS_MESSAGES(148, "settings.disableBossMessages", null, false),
    SHOW_SWORD_KILLS(149, "settings.showSwordKills", new FeatureGuiData(ColorCode.RED, true), false),
    HIDE_OTHER_PLAYERS_PRESENTS(150, "settings.hideOtherPlayersPresents", null, false),
    COMPACT_TAB_LIST(152, "settings.compactTabList", null, false),
    CANDY_POINTS_COUNTER(155, "settings.candyPointsCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD), false),
    SHOW_EXPERIMENTATION_TABLE_TOOLTIPS(158, "settings.showExperimentationTableTooltips", null, true),
    DRILL_FUEL_BAR(160, "settings.drillFuelBar", new FeatureGuiData(DrawType.BAR, ColorCode.DARK_GREEN), false),
    DRILL_FUEL_TEXT(161, "settings.drillFuelNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_GREEN), false),
    FISHING_PARTICLE_OVERLAY(162, "settings.fishingParticleOverlay", new FeatureGuiData(ColorCode.WHITE), false),
    REFORGE_FILTER(172, "settings.reforgeFilter", null, false),
    TREVOR_THE_TRAPPER_FEATURES(177, "settings.trevorTheTrapper.title", new FeatureGuiData(DrawType.PROXIMITY_INDICATOR), false),
    FETCHUR_TODAY(178, "settings.fetchurToday", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN), false),
    STOP_RAT_SOUNDS(183, "settings.stopRatSounds", null, true),
    VOIDGLOOM_SLAYER_TRACKER(186, "settings.voidgloomSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    HIDE_PLAYERS_NEAR_NPCS(190, "settings.hidePlayersNearNPCs", null, false),
    OVERFLOW_MANA(191, "settings.showOverflowManaNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA), false),
    DOUBLE_WARP(192, "settings.doubleWarp", null, true),
    DISABLE_EMPTY_GLASS_PANES(195, "settings.hideMenuGlassPanes", null, false),
    ENTITY_OUTLINES(196, "settings.entityOutlines", null, false),
    EFFECTIVE_HEALTH_TEXT(197, "settings.effectiveHealthNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_GREEN), false),
    OTHER_DEFENCE_STATS(199, "settings.otherDefenseStats", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN), false),
    // Release v1.6
    PREVENT_MOVEMENT_ON_DEATH(200, "settings.preventMovementOnDeath", null, true),
    HIDE_SPAWN_POINT_PLAYERS(201, "settings.hideSpawnPointPlayers", null, true),
    SPIRIT_SCEPTRE_DISPLAY(202, "settings.showSpiritSceptreDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY), false),
    FARM_EVENT_TIMER(204, "settings.jacobsContestTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD), false),
    OUTBID_ALERT_SOUND(206, "settings.outbidAlertSound", null, true),
    BROOD_MOTHER_ALERT(207, "settings.broodMotherWarning", null, false),
    BAL_BOSS_ALERT(208, "settings.balBossWarning", null, false),
    BACKPACK_OPENING_SOUND(211, "settings.backpackOpeningSound", null, false),
    DEVELOPER_MODE(212, "settings.devMode", null, true),
    SHOW_SKYBLOCK_ITEM_ID(213, "settings.showSkyblockItemId", null, true),
    // Release 1.7
    PLAYER_SYMBOLS_IN_CHAT(216, "settings.showPlayerSymbolsInChat", null, false),
    CRIMSON_ARMOR_ABILITY_STACKS(217, "settings.crimsonArmorAbilityStacks", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD), false),
    HIDE_TRUE_DEFENSE(218, "settings.hideTrueDefense", new FeatureGuiData(ColorCode.RED), false),
    // Release Fix3dll
    INFERNO_SLAYER_TRACKER(223, "settings.infernoSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    RIFTSTALKER_SLAYER_TRACKER(228, "settings.riftstalkerSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false),
    FIRE_FREEZE_TIMER(238, "settings.fireFreezeTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW), false),
    HIDE_HAUNTED_SKULLS(241, "settings.hideHauntedSkulls", null, true),
    THUNDER_BOTTLE_DISPLAY(242, "settings.thunderBottleDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_PURPLE), false),
    PET_DISPLAY(246, "settings.petDisplay", new FeatureGuiData(DrawType.PET_DISPLAY, ColorCode.GOLD), false),


    WARNING_TIME(-2, "settings.warningDuration", false),
    LANGUAGE(-3, "language", false),
    EDIT_LOCATIONS(-4, "settings.editLocations", false),
    RESET_LOCATION(-5, "settings.resetLocations",false),
    RESCALE_FEATURES(-6, "messages.rescaleFeatures", false),
    GENERAL_SETTINGS(-7, "settings.tab.generalSettings", false),
    TEXT_STYLE(-8, "settings.textStyle", false),
    CHROMA_SPEED(-9, "settings.chromaSpeed", false),
    CHROMA_MODE(-10, "settings.chromaMode", false),
    CHROMA_SIZE(-11, "settings.chromaSize", false),
    CHROMA_SATURATION(-12, "settings.chromaSaturation", false),
    CHROMA_BRIGHTNESS(-13, "settings.chromaBrightness",  false),
    TURN_ALL_FEATURES_CHROMA(-14, "settings.turnAllFeaturesChroma",  false),
    NUMBER_SEPARATORS(221, "settings.numberSeparators", false),
    TURN_ALL_TEXTS_CHROMA(243, "settings.turnAllTextsChroma", true),
    ENABLE_FEATURE_SNAPPING(254, "messages.enableFeatureSnapping", false),
    SHOW_COLOR_ICONS(256, "messages.showColorIcons", false)
    ;

    private static final LinkedHashSet<Feature> guiFeatures = new LinkedHashSet<>();

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter
    private static final LinkedHashSet<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE,
            WARNING_TIME, CHROMA_SPEED, CHROMA_MODE, CHROMA_SIZE, TURN_ALL_FEATURES_CHROMA, CHROMA_SATURATION,
            CHROMA_BRIGHTNESS, NUMBER_SEPARATORS, DEVELOPER_MODE, TURN_ALL_TEXTS_CHROMA, SBA_BUTTON_IN_PAUSE_MENU
    ));

    @Getter
    private static final LinkedHashSet<Feature> editGuiFeatures = new LinkedHashSet<>(Arrays.asList(
            RESET_LOCATION, RESCALE_FEATURES, SHOW_COLOR_ICONS, ENABLE_FEATURE_SNAPPING
    ));

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final int ID_AT_PREVIOUS_UPDATE = 221;

    private final int id;
    private final FeatureGuiData featureGuiData;
    private final FeatureData<?> featureData;
    private final boolean defaultDisabled;
    private final String translationKey;

    Feature(int id, String translationKey, boolean defaultDisabled) {
        this(id, translationKey, null, defaultDisabled);
    }

    Feature(int id, String translationKey, FeatureGuiData featureGuiData, boolean defaultDisabled) {
        this.id = id;
        this.translationKey = translationKey;
        this.featureGuiData = featureGuiData;
        this.defaultDisabled = defaultDisabled;

        HashSet<Integer> registeredFeatureIDs = SkyblockAddons.getInstance().getRegisteredFeatureIDs();
        if (id != -1 && registeredFeatureIDs.contains(id)) {
            throw new RuntimeException("Multiple features have the same IDs!");
        } else {
            registeredFeatureIDs.add(id);
        }

        this.featureData = new FeatureData<>(featureGuiData);
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
        if (this.getValue() instanceof Boolean) {
            this.setValue(enabled);
            this.onToggle();
            SkyblockAddons.getInstance().getConfigValuesManager().saveConfig();
        } else {
            throw new IllegalStateException(this.name() + " value is not a boolean! Type: " + this.getValue().getClass());
        }
    }

    public boolean isActualFeature() {
        return id != -1 && getMessage() != null;
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
        return this.featureGuiData != null;
    }

    /**
     * Features that are considered GUI ones. This is used for example when saving the config to ensure that these
     * features' coordinates and colors are handled properly.
     */
    public static LinkedHashSet<Feature> getGuiFeatures() {
        if (guiFeatures.isEmpty()) {
            for (Feature feature : values()) {
                if (feature.featureGuiData != null) {
                    guiFeatures.add(feature);
                }
            }
        }

        return guiFeatures;
    }

    public boolean isColorFeature() {
        return this.isGuiFeature() && featureGuiData.getDefaultColor() != null;
    }

    public boolean couldBeXAllignment() {
        if (!this.isGuiFeature() || featureGuiData.getDrawType() == null) return false;

        switch (featureGuiData.getDrawType()) {
            case TEXT:
            case SLAYER_ARMOR_PROGRESS:
            case DEPLOYABLE_DISPLAY:
            case BAIT_LIST_DISPLAY:
            case SLAYER_TRACKERS:
            case DRAGON_STATS_TRACKER:
            case PET_DISPLAY:
                return true;
            case SKELETON_BAR:
            case BAR:
            case PICKUP_LOG:
            case TICKER:
            case DUNGEONS_MAP:
            case PROXIMITY_INDICATOR:
            case DEFENCE_ICON:
            default:
                return false;
        }
    }

    public ColorCode getDefaultColor() {
        if (featureGuiData != null) {
            return featureGuiData.getDefaultColor();
        }
        return null;
    }

    public boolean isNew() {
        return id > ID_AT_PREVIOUS_UPDATE;
    }

    public boolean isEnabled() {
        if (this.getValue() instanceof Boolean) {
            return (boolean) this.getValue();
        }
        throw new IllegalStateException(this.name() + " is not a boolean! Type: " + this.getValue().getClass());
    }

    public boolean isDisabled() {
        return !this.isEnabled();
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

    public Object getValue() {
        return this.featureData.getValue();
    }

    public Number numberValue() {
        Object value = this.getValue();
        if (value instanceof Number) {
            return (Number) value;
        }
        throw new IllegalStateException(this.name() + " value is not a number!");
    }

    public void setValue(Object value) {
        if (FeatureData.isValidValue(value)) {
            this.featureData.setValue(value);
        } else {
            throw new IllegalArgumentException(value + " is not valid for '" + this.name() + "'!");
        }
    }

    public AnchorPoint getAnchorPoint() {
        return this.featureData.getAnchorPoint();
    }

    public float getGuiScale() {
        return this.featureData.getGuiScale();
    }

    /**
     * Whatever you set the scale factor to, the maximum limit will be set to {@code 5.0F} and the minimum limit to {@code 0.5F}
     * @param scale Float GUI Scale
     */
    public void setGuiScale(float scale) {
        this.featureData.setGuiScale(Math.max(Math.min(scale, 5.0F), 0.5F));
    }

    public int getColor() {
        int color = this.featureData.getColor();

        if (this.isChroma()) {
            return ManualChromaManager.getChromaColor(0, 0, ColorUtils.getAlpha(color));
        }

        return color;
    }

    /**
     * Returns Feature's color with provided alpha.
     * @param alpha Integer Alpha Value (8-bit)
     * @return {@code alpha} applied Feature's color
     */
    public int getColor(int alpha) {
        int color = this.featureData.getColor();

        if (this.isChroma()) {
            return ManualChromaManager.getChromaColor(0, 0, alpha);
        }

        return ColorUtils.setColorAlpha(color, alpha);
    }

    public void setColor(int color) {
        this.featureData.setColor(color);
    }

    public ColorCode getRestrictedColor() {
        int featureColor = this.getColor();

        for (ColorCode colorCode : ColorCode.values()) {
            if (!colorCode.isColor()) {
                continue;
            }

            if (colorCode.getColor() == featureColor) {
                return colorCode;
            }
        }

        return this.getDefaultColor();
    }

    /**
     * Return skyblock color compatible with new shaders. Can bind the color (white) unconditionally
     * @return the color
     */
    // TODO merge with ColorCode
    public SkyblockColor getSkyblockColor() {
        SkyblockColor color = ColorUtils.getDummySkyblockColor(this.getColor(), this.isChroma());
        // If chroma is enabled, and we are using shaders, set color to white
        if (color.drawMulticolorUsingShader()) {
            color.setColor(0xFFFFFFFF);
        }
        return color;
    }

    public boolean isChroma() {
        return this.featureData.isChroma();
    }

    public void setChroma(boolean chroma) {
        this.featureData.setChroma(chroma);
    }

    public boolean hasSettings() {
        return this.featureData.getSettings() != null && !this.featureData.getSettings().isEmpty();
    }

    public int settingsSize() {
        return this.hasSettings() ? this.featureData.getSettings().size() : 0;
    }

    /**
     * Checks whether the specified is enabled. If the {@link Feature} is not enabled or doesn't have settings map,
     * it returns {@code false} for all related {@link FeatureSetting}s.
     * @param setting Feature related setting
     * @return true if the {@code setting} is enabled
     * @exception IllegalArgumentException if {@code setting} is not related with this Feature
     */
    public boolean isEnabled(FeatureSetting setting) {
        if (setting.getRelatedFeature() != this && !setting.isUniversal()) {
            throw new IllegalArgumentException(setting.getRelatedFeature() + " is not a related with " + this.name());
        } else if (this.isDisabled()) {
            // Feature must be enabled before the check FeatureSetting
            return false;
        } else if (!this.hasSettings()) {
            if (DEVELOPER_MODE.isEnabled()) LOGGER.error("{} doesn't have FeatureSettings!", this.name());
            return false;
        } else if (!this.featureData.getSettings().containsKey(setting)) { // For debug reason, not actually needed
            if (DEVELOPER_MODE.isEnabled()) LOGGER.error("{} does not contain setting '{}'!", this.name(), setting.name());
            return false;
        }

        return Boolean.TRUE.equals(this.featureData.getSettings().get(setting));
    }

    /**
     * @param setting Feature related setting
     * @return opposite of isEnabled()
     * @see Feature#isEnabled()
     */
    public boolean isDisabled(FeatureSetting setting) {
        return !isEnabled(setting);
    }

    /**
     * Returns setting value as {@link Number}
     * @param setting Feature related setting
     * @return The value of the {@code setting} as {@link Number}
     * @exception IllegalArgumentException if specified setting value is not instance of {@link Number}
     * @see Feature#get(FeatureSetting)
     */
    public @NonNull Number getAsNumber(FeatureSetting setting) {
        Object value = this.get(setting);
        if (value instanceof Number) {
            return (Number) value;
        }
        throw new IllegalArgumentException("Setting " + setting + " is not a number. Type: " + value.getClass());
    }

    /**
     * Returns setting value as {@link RegistrableEnum}
     * @param setting Feature related setting
     * @return The value of the {@code setting} as {@link RegistrableEnum}
     * @exception IllegalArgumentException if specified setting value is not instance of {@link RegistrableEnum}
     * @see Feature#get(FeatureSetting)
     */
    public @NonNull RegistrableEnum getAsEnum(FeatureSetting setting) {
        Object value = this.get(setting);
        if (value instanceof RegistrableEnum) {
            return (RegistrableEnum) value;
        }
        throw new IllegalArgumentException("Setting " + setting + " is not a RegistrableEnum. Type: " + value.getClass());
    }

    /**
     * Returns setting value as {@link String}
     * @param setting Feature related setting
     * @return The value of the {@code setting} as {@link String}
     * @exception IllegalArgumentException if specified setting value is not instance of {@link String}
     * @see Feature#get(FeatureSetting)
     */
    public @NonNull String getAsString(FeatureSetting setting) {
        Object value = this.get(setting);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Setting " + setting + " is not a string. Type: " + value.getClass());
    }

    /**
     * If the Feature has a setting map and the specified setting is associated with this Feature, it returns the stored
     * value of this setting. Universal settings can bypass these exceptions.
     * @param setting Feature related setting
     * @return The value of the {@code setting} in {@link Object}
     * @exception IllegalArgumentException if {@code setting} is not related with this Feature
     * @exception IllegalStateException if Feature doesn't have setting map or not contains {@code setting}
     */
    public @NonNull Object get(FeatureSetting setting) {
        if (setting.getRelatedFeature() != this && !setting.isUniversal()) {
            throw new IllegalArgumentException(setting.getRelatedFeature() + " is not a related with " + this.name());
        } else if (!this.hasSettings()) {
            throw new IllegalStateException(this.name() +  " doesn't have FeatureSettings!");
        } else if (!this.featureData.getSettings().containsKey(setting)) {
            throw new IllegalStateException(this.name() + " does not contain setting '" + setting.name() + "'!");
        }

        return this.featureData.getSettings().get(setting);
    }

    /**
     * Sets the value of the specified setting. If the setting is Universal and the specified value is valid, exceptions
     * can be bypassed and saved. If the setting map does not exist, a new map is created for the Universal setting.
     * @param setting Feature related setting
     * @param value value to be associated with the specified setting
     * @exception IllegalArgumentException if {@code setting} is not related with this Feature
     * @exception IllegalStateException if Feature doesn't have setting map or not contains {@code setting}
     * @exception IllegalStateException if specified value is not valid
     * @see FeatureData#isValidValue(Object)
     */
    public <T> void set(FeatureSetting setting, T value) {
        if (!setting.isUniversal()) {
            if (setting.getRelatedFeature() != this) {
                throw new IllegalArgumentException(setting.getRelatedFeature() + " is not a related with " + this.name());
            } else if (!this.hasSettings()) {
                throw new IllegalStateException(this.name() + " doesn't have FeatureSettings!");
            } else if (!this.featureData.getSettings().containsKey(setting)) {
                throw new IllegalStateException(this.name() + " does not contain setting '" + setting.name() + "'!");
            }
        } else if (this.featureData.getSettings() == null) {
            // If settings map is null, create new one for universal settings
            this.featureData.setSettings(new TreeMap<>());
        }

        if (FeatureData.isValidValue(value)) {
            this.featureData.getSettings().put(setting, value);
        } else {
            throw new IllegalStateException("Tried to set invalid value to '" + setting.name() + "'. Value type: " + value.getClass());
        }
    }

    /**
     * Checks whether the specified {@link FeatureSetting} is present.
     * @param setting of Feature
     * @return true if {@link Feature}'s settings is not null and contains {@code setting}
     */
    public boolean has(@NonNull FeatureSetting setting) {
        if (this.featureData.getSettings() == null) {
            return false;
        } else {
            return this.featureData.getSettings().containsKey(setting);
        }
    }
}