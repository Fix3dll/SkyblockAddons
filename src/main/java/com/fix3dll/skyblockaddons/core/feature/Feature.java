package com.fix3dll.skyblockaddons.core.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.ConfigValuesManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.utils.EnumUtils.AnchorPoint;
import com.fix3dll.skyblockaddons.utils.EnumUtils.DrawType;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

/**
 * @implNote Please don't forget to add new Feature or FeatureSetting values to {@code defaults.json} before push.
 */
@Getter
public enum Feature {
    DROP_CONFIRMATION(1, "settings.itemDropConfirmation", new FeatureGuiData(ColorCode.RED)),
    SHOW_BACKPACK_PREVIEW(3, "settings.showBackpackPreview", null),
    HIDE_BONES(4, "settings.hideSkeletonHatBones", null),
    SKELETON_BAR(5, "settings.skeletonHatBonesBar", new FeatureGuiData(DrawType.SKELETON_BAR)),
    HIDE_FOOD_ARMOR_BAR(6, "settings.hideFoodAndArmor", null),
    FULL_INVENTORY_WARNING(7, "settings.fullInventoryWarning", new FeatureGuiData(ColorCode.RED)),
    SHOW_REFORGE_OVERLAY(10, "settings.showReforgeOverlay", null),
    MINION_STOP_WARNING(11, "settings.minionStopWarning", new FeatureGuiData(ColorCode.RED)),
    HIDE_HEALTH_BAR(13, "settings.hideHealthBar", null),
    MINION_FULL_WARNING(15, "settings.fullMinionWarning", new FeatureGuiData(ColorCode.RED)),
    MANA_BAR(19, "settings.manaBar", new FeatureGuiData(DrawType.BAR, ColorCode.AQUA)),
    MANA_TEXT(20, "settings.manaNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA)),
    HEALTH_BAR(21, "settings.healthBar", new FeatureGuiData(DrawType.BAR, ColorCode.RED)),
    HEALTH_TEXT(22, "settings.healthNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.RED)),
    DEFENCE_ICON(23, "settings.defenseIcon", new FeatureGuiData(DrawType.DEFENCE_ICON)),
    DEFENCE_TEXT(24, "settings.defenseNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN)),
    DEFENCE_PERCENTAGE(25, "settings.defensePercentage", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN)),
    HEALTH_UPDATES(26, "settings.healthUpdates", new FeatureGuiData(DrawType.TEXT)),
    HIDE_PLAYERS_IN_LOBBY(27, "settings.hidePlayersInLobby", null),
    DARK_AUCTION_TIMER(28, "settings.darkAuctionTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD)),
    ITEM_PICKUP_LOG(29, "settings.itemPickupLog", new FeatureGuiData(DrawType.PICKUP_LOG)),
    DONT_RESET_CURSOR_INVENTORY(37, "settings.dontResetCursorInventory", null),
    LOCK_SLOTS(38, "settings.lockSlots", null),
    SUMMONING_EYE_ALERT(39, "settings.summoningEyeAlert", new FeatureGuiData(ColorCode.RED)),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, "settings.makeEnderchestsInEndGreen", new FeatureGuiData(ColorCode.GREEN)),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, "settings.stopDroppingSellingRareItems", new FeatureGuiData(ColorCode.RED)),
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, "settings.replaceRomanNumeralsWithNumbers", null),
    FISHING_SOUND_INDICATOR(48, "settings.soundIndicatorForFishing", null),
    AVOID_BLINKING_NIGHT_VISION(49, "settings.avoidBlinkingNightVision", null), // TODO remove
    MINION_DISABLE_LOCATION_WARNING(50, "settings.disableMinionLocationWarning", null),
    ENCHANTMENT_LORE_PARSING(52, "settings.enchantmentLoreParsing", null),
    SHOW_ITEM_COOLDOWNS(53, "settings.showItemCooldowns", null),
    SKILL_DISPLAY(54, "settings.collectionDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA)),
    SPEED_PERCENTAGE(55, "settings.speedPercentage", new FeatureGuiData(DrawType.TEXT, ColorCode.WHITE)),
    SLAYER_ARMOR_PROGRESS(57, "settings.revenantIndicator", new FeatureGuiData(DrawType.SLAYER_ARMOR_PROGRESS, ColorCode.AQUA)),
    SPECIAL_ZEALOT_ALERT(58, "settings.specialZealotAlert", new FeatureGuiData(ColorCode.RED)),
    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null),
    HIDE_PET_HEALTH_BAR(63, "settings.hidePetHealthBar", null),
    // Release v1.4
    DISABLE_MAGICAL_SOUP_MESSAGES(64, "settings.disableMagicalSoupMessage", null),
    DEPLOYABLE_STATUS_DISPLAY(65, "settings.deployableDisplay", new FeatureGuiData(DrawType.DEPLOYABLE_DISPLAY, null)),
    ZEALOT_COUNTER(66, "settings.zealotCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    TICKER_CHARGES_DISPLAY(67, "settings.tickerChargesDisplay", new FeatureGuiData(DrawType.TICKER, null)),
    NO_ARROWS_LEFT_ALERT(69, "settings.noArrowsLeftAlert", null),
    SBA_BUTTON_IN_PAUSE_MENU(76, "settings.skyblockAddonsButtonInPauseMenu", null),
    SHOW_TOTAL_ZEALOT_COUNT(77, "settings.showTotalZealotCount", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    SHOW_SUMMONING_EYE_COUNT(78, "settings.showSummoningEyeCount", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    SHOW_AVERAGE_ZEALOTS_PER_EYE(79, "settings.showZealotsPerEye", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    TURN_BOW_COLOR_WHEN_USING_ARROW_POISON(80, "settings.turnBowGreenWhenUsingToxicArrowPoison", null),
    BIRCH_PARK_RAINMAKER_TIMER(81, "settings.birchParkRainmakerTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    DISCORD_RPC(83, "settings.discordRP", null),
    ENDSTONE_PROTECTOR_DISPLAY(84, "settings.endstoneProtectorDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.WHITE)),
    FANCY_WARP_MENU(85, "settings.fancyWarpMenu", null),
    LEGENDARY_SEA_CREATURE_WARNING(88, "settings.legendarySeaCreatureWarning", new FeatureGuiData(ColorCode.RED)),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(90, null),
    BOSS_APPROACH_ALERT(91, "settings.bossApproachAlert", null),
    DISABLE_TELEPORT_PAD_MESSAGES(92, "settings.disableTeleportPadMessages", null),
    BAIT_LIST(93, "settings.baitListDisplay", new FeatureGuiData(DrawType.BAIT_LIST_DISPLAY, ColorCode.AQUA)),
    ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(94, "settings.zealotCounterExplosiveBow", null),
    DISABLE_ENDERMAN_TELEPORTATION_EFFECT(95, "settings.disableEndermanTeleportation", null),
    CHANGE_ZEALOT_COLOR(96, "settings.changeZealotColor", new FeatureGuiData(ColorCode.LIGHT_PURPLE)),
    HIDE_SVEN_PUP_NAMETAGS(97, "settings.hideSvenPupNametags", null),
    // Release v1.5
    DUNGEONS_MAP_DISPLAY(99, "settings.dungeonMapDisplay", new FeatureGuiData(DrawType.DUNGEONS_MAP, ColorCode.BLACK)),
    MAKE_DROPPED_ITEMS_GLOW(102, "settings.glowingDroppedItems", null),
    SHOW_BASE_STAT_BOOST_PERCENTAGE(104, "settings.baseStatBoostPercentage", new FeatureGuiData(ColorCode.RED)),
    SHOW_HEALING_CIRCLE_WALL(107, "settings.showHealingCircleWall", new FeatureGuiData(ColorCode.GREEN)),
    SHOW_CRITICAL_DUNGEONS_TEAMMATES(108, "settings.showCriticalTeammates", null),
    SHOW_ITEM_DUNGEON_FLOOR(110, "settings.showItemDungeonFloor", new FeatureGuiData(ColorCode.RED)),
    SHOW_DUNGEON_MILESTONE(111, "settings.showDungeonMilestone", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW)),
    DUNGEONS_COLLECTED_ESSENCES_DISPLAY(112, "settings.dungeonsCollectedEssencesDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW)),
    STOP_BONZO_STAFF_SOUNDS(113, "settings.stopBonzoStaffSounds", null),
    REVENANT_SLAYER_TRACKER(116, "settings.revenantSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    TARANTULA_SLAYER_TRACKER(117, "settings.tarantulaSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    SVEN_SLAYER_TRACKER(118, "settings.svenSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    DRAGON_STATS_TRACKER(125, "settings.dragonStatsTracker", new FeatureGuiData(DrawType.DRAGON_STATS_TRACKER, ColorCode.WHITE)),
    DUNGEON_DEATH_COUNTER(136, "settings.dungeonDeathCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.RED)),
    ROCK_PET_TRACKER(138, "settings.rockPetTracker", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY)),
    DOLPHIN_PET_TRACKER(139, "settings.dolphinPetTracker", new FeatureGuiData(DrawType.TEXT, ColorCode.AQUA)),
    SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY(140, "settings.dungeonsTeammateNameOverlay", null),
    SHOW_STACKING_ENCHANT_PROGRESS(141, "settings.stackingEnchantProgress", new FeatureGuiData(ColorCode.RED)),
    DUNGEONS_SECRETS_DISPLAY(142, "settings.dungeonsSecretsDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY)),
    SKILL_PROGRESS_BAR(143, "settings.skillProgressBar", new FeatureGuiData(DrawType.BAR, ColorCode.GREEN)),
    DISABLE_MORT_MESSAGES(147, "settings.disableMortMessages", null),
    DISABLE_BOSS_MESSAGES(148, "settings.disableBossMessages", null),
    SHOW_SWORD_KILLS(149, "settings.showSwordKills", new FeatureGuiData(ColorCode.RED)),
    HIDE_OTHER_PLAYERS_PRESENTS(150, "settings.hideOtherPlayersPresents", null),
    COMPACT_TAB_LIST(152, "settings.compactTabList", null),
    CANDY_POINTS_COUNTER(155, "settings.candyPointsCounter", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD)),
    SHOW_EXPERIMENTATION_TABLE_TOOLTIPS(158, "settings.showExperimentationTableTooltips", null),
    DRILL_FUEL_BAR(160, "settings.drillFuelBar", new FeatureGuiData(DrawType.BAR, ColorCode.DARK_GREEN)),
    DRILL_FUEL_TEXT(161, "settings.drillFuelNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_GREEN)),
    FISHING_PARTICLE_OVERLAY(162, "settings.fishingParticleOverlay", new FeatureGuiData(ColorCode.WHITE)),
    REFORGE_FILTER(172, "settings.reforgeFilter", null),
    TREVOR_THE_TRAPPER_FEATURES(177, "settings.trevorTheTrapper.title", new FeatureGuiData(DrawType.PROXIMITY_INDICATOR)),
    FETCHUR_TODAY(178, "settings.fetchurToday", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN)),
    STOP_RAT_SOUNDS(183, "settings.stopRatSounds", null),
    VOIDGLOOM_SLAYER_TRACKER(186, "settings.voidgloomSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    HIDE_PLAYERS_NEAR_NPCS(190, "settings.hidePlayersNearNPCs", null),
    OVERFLOW_MANA(191, "settings.showOverflowManaNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_AQUA)),
    DISABLE_EMPTY_GLASS_PANES(195, "settings.hideMenuGlassPanes", null),
    ENTITY_OUTLINES(196, "settings.entityOutlines", null),
    EFFECTIVE_HEALTH_TEXT(197, "settings.effectiveHealthNumber", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_GREEN)),
    OTHER_DEFENCE_STATS(199, "settings.otherDefenseStats", new FeatureGuiData(DrawType.TEXT, ColorCode.GREEN)),
    // Release v1.6
    PREVENT_MOVEMENT_ON_DEATH(200, "settings.preventMovementOnDeath", null),
    HIDE_SPAWN_POINT_PLAYERS(201, "settings.hideSpawnPointPlayers", null),
    SPIRIT_SCEPTRE_DISPLAY(202, "settings.showSpiritSceptreDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.GRAY)),
    FARM_EVENT_TIMER(204, "settings.jacobsContestTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD)),
    OUTBID_ALERT_SOUND(206, "settings.outbidAlertSound", null),
    BROOD_MOTHER_ALERT(207, "settings.broodMotherWarning", null),
    BAL_BOSS_ALERT(208, "settings.balBossWarning", null),
    BACKPACK_OPENING_SOUND(211, "settings.backpackOpeningSound", null),
    DEVELOPER_MODE(212, "settings.devMode", null),
    SHOW_SKYBLOCK_ITEM_ID(213, "settings.showSkyblockItemId", null),
    // Release 1.7
    PLAYER_SYMBOLS_IN_CHAT(216, "settings.showPlayerSymbolsInChat", null),
    CRIMSON_ARMOR_ABILITY_STACKS(217, "settings.crimsonArmorAbilityStacks", new FeatureGuiData(DrawType.TEXT, ColorCode.GOLD)),
    HIDE_TRUE_DEFENSE(218, "settings.hideTrueDefense", new FeatureGuiData(ColorCode.RED)),
    // Release Fix3dll
    INFERNO_SLAYER_TRACKER(223, "settings.infernoSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    RIFTSTALKER_SLAYER_TRACKER(228, "settings.riftstalkerSlayerTracker", new FeatureGuiData(DrawType.SLAYER_TRACKERS, ColorCode.WHITE)),
    FIRE_FREEZE_TIMER(238, "settings.fireFreezeTimer", new FeatureGuiData(DrawType.TEXT, ColorCode.YELLOW)),
    HIDE_HAUNTED_SKULLS(241, "settings.hideHauntedSkulls", null),
    THUNDER_BOTTLE_DISPLAY(242, "settings.thunderBottleDisplay", new FeatureGuiData(DrawType.TEXT, ColorCode.DARK_PURPLE)),
    PET_DISPLAY(246, "settings.petDisplay", new FeatureGuiData(DrawType.PET_DISPLAY, ColorCode.GOLD)),
    PRESSURE_BAR(260, "settings.pressureBar.title", new FeatureGuiData(DrawType.BAR, ColorCode.BLUE)),
    PRESSURE_TEXT(261, "settings.pressureText.title", new FeatureGuiData(DrawType.TEXT, ColorCode.BLUE)),
    HIDE_EFFECTS_HUD(262, "settings.hideEffectsHud", null),
    EQUIPMENTS_IN_INVENTORY(263, "settings.equipmentsInInventory.title", new FeatureGuiData(ColorCode.WHITE)),


    WARNING_TIME(-2, "settings.warningDuration"),
    LANGUAGE(-3, "language"),
    EDIT_LOCATIONS(-4, "settings.editLocations"),
    RESET_LOCATION(-5, "settings.resetLocations"),
    RESCALE_FEATURES(-6, "messages.rescaleFeatures"),
    GENERAL_SETTINGS(-7, "settings.tab.generalSettings"),
    TEXT_STYLE(-8, "settings.textStyle"),
    CHROMA_SPEED(-9, "settings.chromaSpeed"),
    CHROMA_MODE(-10, "settings.chromaMode"),
    CHROMA_SIZE(-11, "settings.chromaSize"),
    CHROMA_SATURATION(-12, "settings.chromaSaturation"),
    CHROMA_BRIGHTNESS(-13, "settings.chromaBrightness"),
    TURN_ALL_FEATURES_CHROMA(-14, "settings.turnAllFeaturesChroma"),
    NUMBER_SEPARATORS(221, "settings.numberSeparators"),
    TURN_ALL_TEXTS_CHROMA(243, "settings.turnAllTextsChroma"),
    ENABLE_FEATURE_SNAPPING(254, "messages.enableFeatureSnapping"),
    SHOW_COLOR_ICONS(256, "messages.showColorIcons"),
    AUTO_UPDATE(257, "settings.autoUpdate.title"),
    FULL_AUTO_UPDATE(258, "settings.fullAutoUpdate"),
    CHAT_MESSAGE_COPYING(259, "settings.chatMessageCopying"),
    ;

    private static final LinkedHashSet<Feature> guiFeatures = new LinkedHashSet<>();

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter
    private static final LinkedHashSet<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE,
            WARNING_TIME, CHROMA_SPEED, CHROMA_MODE, CHROMA_SIZE, TURN_ALL_FEATURES_CHROMA, CHROMA_SATURATION,
            CHROMA_BRIGHTNESS, NUMBER_SEPARATORS, DEVELOPER_MODE, TURN_ALL_TEXTS_CHROMA, SBA_BUTTON_IN_PAUSE_MENU,
            AUTO_UPDATE, FULL_AUTO_UPDATE, CHAT_MESSAGE_COPYING
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
    private final String translationKey;

    Feature(int id, String translationKey) {
        this(id, translationKey, null);
    }

    Feature(int id, String translationKey, FeatureGuiData featureGuiData) {
        this.id = id;
        this.translationKey = translationKey;
        this.featureGuiData = featureGuiData;

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
    public void onToggle(boolean enabled) {
        if (this == DEVELOPER_MODE) {
            if (enabled) {
                SkyblockKeyBinding.DEVELOPER_COPY_NBT.register(Minecraft.getInstance().options);
            } else {
                SkyblockKeyBinding.DEVELOPER_COPY_NBT.deRegister();
            }
        }

        if (this == TURN_ALL_TEXTS_CHROMA) {
            FontHook.setAllTextChroma(enabled);
        }
    }

    /**
     * Sets whether the current feature is enabled.
     * @param enabled {@code true} to enable the feature, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        ConfigValuesManager cvm = SkyblockAddons.getInstance().getConfigValuesManager();
        Object value = this.getValue();

        if (value instanceof Boolean) {
            this.setValue(enabled);
            this.onToggle(enabled);
            cvm.saveConfig();
        } else {
            cvm.restoreFeatureDefaultValue(this);
            throw new IllegalStateException(this + " value is not a boolean! Type: " + value);
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

        return switch (featureGuiData.getDrawType()) {
            case TEXT,
                 SLAYER_ARMOR_PROGRESS,
                 DEPLOYABLE_DISPLAY,
                 BAIT_LIST_DISPLAY,
                 SLAYER_TRACKERS,
                 DRAGON_STATS_TRACKER,
                 PET_DISPLAY -> true;
            default -> false;
        };
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
        Object value = this.getValue();

        if (value instanceof Boolean bool) {
            return bool && !isRemoteDisabled();
        }
        SkyblockAddons.getInstance().getConfigValuesManager().restoreFeatureDefaultValue(this);
        throw new IllegalStateException(this + " is not a boolean! Type: " + value);
    }

    public boolean isDisabled() {
        return !this.isEnabled();
    }

    private static final String VERSION_X_Y_Z = SkyblockAddons.METADATA.getVersion().toString().split("-")[0];

    /**
     * Checks the received {@code OnlineData} to determine if the given feature should be disabled.This method checks
     * the list of features to be disabled for all versions first and then checks the list of features that should be
     * disabled for this specific version.
     * @return {@code true} if the feature should be disabled, {@code false} otherwise
     */
    public boolean isRemoteDisabled() {
        HashMap<String, List<Integer>> disabledFeatures = SkyblockAddons.getInstance().getOnlineData().getDisabledFeatures();

        if (disabledFeatures.containsKey("all")) {
            List<Integer> allList = disabledFeatures.get("all");
            if (allList != null && allList.contains(this.getId())) {
                return true;
            } else if (allList == null) {
                LOGGER.error("\"all\" key in disabled features map has value of null. Please fix online data.");
            }
        }

        /*
        Check for disabled features for this mod version. Pre-release versions will follow the disabled features
        list for their release version. For example, the version {@code 1.6.0-beta.10} will adhere to the list
        for version {@code 1.6.0}
         */
        if (disabledFeatures.containsKey(VERSION_X_Y_Z)) {
            List<Integer> versionList = disabledFeatures.get(VERSION_X_Y_Z);
            if (versionList != null && versionList.contains(this.getId())) {
                return true;
            } else if (versionList == null) {
                LOGGER.error("\"{}\" key in disabled features map has value of null. Please fix online data.", VERSION_X_Y_Z);
            }
        }

        return false;
    }

    public Object getValue() {
        return this.featureData.getValue();
    }

    public Number numberValue() {
        Object value = this.getValue();
        if (value instanceof Number number) {
            return number;
        }
        SkyblockAddons.getInstance().getConfigValuesManager().restoreFeatureDefaultValue(this);
        throw new IllegalStateException(this + " value is not a number!");
    }

    public void setValue(Object value) {
        if (FeatureData.isValidValue(value)) {
            this.featureData.setValue(value);
        } else {
            SkyblockAddons.getInstance().getConfigValuesManager().restoreFeatureDefaultValue(this);
            throw new IllegalArgumentException(value + " is not valid for '" + this + "'!");
        }
    }

    public AnchorPoint getAnchorPoint() {
        AnchorPoint anchorPoints = this.featureData.getAnchorPoint();
        if (anchorPoints != null) {
            return anchorPoints;
        } else {
            anchorPoints = ConfigValuesManager.DEFAULT_FEATURE_DATA.get(this).getAnchorPoint();
            return anchorPoints == null ? AnchorPoint.BOTTOM_MIDDLE : anchorPoints;
        }
    }

    public void setClosestAnchorPoint() {
        float x1 = this.getActualX();
        float y1 = this.getActualY();
        Window window = Minecraft.getInstance().getWindow();
        int maxX = window.getGuiScaledWidth();
        int maxY = window.getGuiScaledHeight();
        double shortestDistance = -1;
        AnchorPoint closestAnchorPoint = AnchorPoint.BOTTOM_MIDDLE; // default
        for (AnchorPoint point : AnchorPoint.values()) {
            double distance = Point2D.distance(x1, y1, point.getX(maxX), point.getY(maxY));
            if (shortestDistance == -1 || distance < shortestDistance) {
                closestAnchorPoint = point;
                shortestDistance = distance;
            }
        }
        if (this.getAnchorPoint() == closestAnchorPoint) {
            return;
        }
        float targetX = this.getActualX();
        float targetY = this.getActualY();
        float x = targetX-closestAnchorPoint.getX(maxX);
        float y = targetY-closestAnchorPoint.getY(maxY);
        this.featureData.setAnchorPoint(closestAnchorPoint);
        this.featureData.setCoords(x, y);
    }

    public float getActualX() {
        int maxX = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return this.getAnchorPoint().getX(maxX) + this.getRelativeCoords().getLeft();
    }

    public float getActualY() {
        int maxY = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return this.getAnchorPoint().getY(maxY) + this.getRelativeCoords().getRight();
    }

    public Pair<Float, Float> getRelativeCoords() {
        Pair<Float, Float> coords = this.featureData.getCoords();
        if (coords != null) {
            return coords;
        } else {
            SkyblockAddons.getInstance().getConfigValuesManager().putDefaultCoordinates(this);
            coords = this.featureData.getCoords();
            return coords == null ? new Pair<>(0F, 0F) : coords;
        }
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
            return ManualChromaManager.getChromaColor(0, 0, ARGB.alpha(color));
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

        return ARGB.color(alpha, color);
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

    public boolean isChroma() {
        return this.featureData.isChroma();
    }

    public void setChroma(boolean chroma) {
        this.featureData.setChroma(chroma);
    }

    public boolean hasSettings() {
        return this.featureData.hasSettings();
    }

    public int settingsSize() {
        return this.hasSettings() ? this.featureData.getSettings().size() : 0;
    }

    /**
     * Checks whether the specified is enabled. If the {@link Feature} is not enabled it returns {@code false}.
     * @param setting Feature related setting
     * @return true if the {@code setting} is enabled
     * @see Feature#get(FeatureSetting)
     */
    public boolean isEnabled(FeatureSetting setting) {
        // Feature must be enabled before the check FeatureSetting
        if (this.isDisabled()) {
            return false;
        }

        return Boolean.TRUE.equals(this.get(setting));
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
        if (value instanceof Number number) {
            return number;
        }
        SkyblockAddons.getInstance().getConfigValuesManager().setSettingToDefault(setting);
        throw new IllegalArgumentException("Setting " + setting + " is not a number. Type: " + value);
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
        if (value instanceof RegistrableEnum registrableEnum) {
            return registrableEnum;
        }
        SkyblockAddons.getInstance().getConfigValuesManager().setSettingToDefault(setting);
        throw new IllegalArgumentException("Setting " + setting + " is not a RegistrableEnum. Type: " + value);
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
        if (value instanceof String string) {
            return string;
        }
        SkyblockAddons.getInstance().getConfigValuesManager().setSettingToDefault(setting);
        throw new IllegalArgumentException("Setting " + setting + " is not a string. Type: " + value);
    }

    /**
     * If the property has a settings map and the specified setting is associated with this property, it returns the
     * stored value of that setting. If the setting is associated with the property but no settings map exists, then the
     * setting value is set to its default value and then the value of the setting is returned.
     * @return The value of the {@code setting} in {@link Object}
     * @exception IllegalArgumentException if {@code setting} is not related with this Feature
     * @see ConfigValuesManager#setSettingToDefault(FeatureSetting)
     */
    public @NonNull Object get(FeatureSetting setting) {
        if (setting.getRelatedFeature() != this && !setting.isUniversal()) {
            throw new IllegalArgumentException(setting.getRelatedFeature() + " is not related to " + this);
        }

        // If there is a FeatureSetting related to the Feature but the settings map is empty, the map must be updated.
        if (!this.hasSettings()) {
            if (setting.isUniversal()) {
                this.featureData.setSettings(new TreeMap<>());
            } else {
                SkyblockAddons.getInstance().getConfigValuesManager().setSettingToDefault(setting);
            }
        }

        return this.featureData.getSettings().get(setting);
    }

    /**
     * If the value is valid, it is set to the value of the specified setting. If the settings map does not exist, the
     * map is created and then the value is added.
     * @param setting Feature related setting
     * @param value value to be associated with the specified setting
     * @exception IllegalArgumentException if {@code setting} is not related with this Feature
     * @exception IllegalStateException if specified value is not valid
     * @see FeatureData#setSetting(FeatureSetting, Object)
     */
    public <T> void set(FeatureSetting setting, T value) {
        if (setting.getRelatedFeature() != this && !setting.isUniversal()) {
            throw new IllegalArgumentException(setting.getRelatedFeature() + " is not related to " + this);
        }

        this.featureData.setSetting(setting, value);
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