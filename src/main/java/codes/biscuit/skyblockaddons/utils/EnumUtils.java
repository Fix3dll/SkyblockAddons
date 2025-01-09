package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonCycling;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Set;

public class EnumUtils {

    @SuppressWarnings("unused")
    @Getter
    public enum AnchorPoint {

        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        BOTTOM_MIDDLE(4),
        TOP_MIDDLE(5);

        private final int id;

        AnchorPoint(int id) {
            this.id = id;
        }

        @SuppressWarnings("unused") // Accessed by reflection...
        public static AnchorPoint fromId(int id) {
            for (AnchorPoint feature : values()) {
                if (feature.getId() == id) {
                    return feature;
                }
            }
            return null;
        }

        public int getX(int maxX) {
            switch (this) {
                case TOP_RIGHT: case BOTTOM_RIGHT:
                    return maxX;
                case TOP_MIDDLE: case BOTTOM_MIDDLE:
                    return maxX / 2;
                default:
                    return 0;
            }
        }

        public int getY(int maxY) {
            switch (this) {
                case BOTTOM_LEFT: case BOTTOM_RIGHT: case BOTTOM_MIDDLE:
                    return maxY;
                default:
                    return 0;
            }
        }

        public boolean isOnTop() {
            return this == TOP_LEFT || this == TOP_RIGHT || this == TOP_MIDDLE;
        }

        public boolean isOnBottom() {
            return this == BOTTOM_LEFT || this == BOTTOM_RIGHT || this == BOTTOM_MIDDLE;
        }

        public boolean isOnLeft() {
            return this == TOP_LEFT || this == BOTTOM_LEFT;
        }

        public boolean isOnRight() {
            return this == TOP_RIGHT || this == BOTTOM_RIGHT;
        }

        public boolean isOnMiddle() {
            return this == TOP_MIDDLE || this == BOTTOM_MIDDLE;
        }
    }

    public enum ButtonType {
        TOGGLE,
        SOLID,
        CHROMA_SLIDER,
        CYCLING,
        STEPPER
    }

    public enum BackpackStyle implements ButtonCycling.SelectItem {
        GUI("settings.backpackStyles.regular"),
        BOX("settings.backpackStyles.compact");

        private final String TRANSLATION_KEY;

        BackpackStyle(String translationKey) {
            this.TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public enum DeployableDisplayStyle implements ButtonCycling.SelectItem {
        DETAILED("settings.deployableStyle.detailed"),
        COMPACT("settings.deployableStyle.compact");

        private final String TRANSLATION_KEY;

        DeployableDisplayStyle(String translationKey) {
            this.TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public enum TextStyle implements ButtonCycling.SelectItem {
        STYLE_ONE("settings.textStyles.one"),
        STYLE_TWO("settings.textStyles.two");

        private final String TRANSLATION_KEY;

        TextStyle(String translationKey) {
            this.TRANSLATION_KEY = translationKey;
        }

        public String getMessage() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public enum GuiTab {
        MAIN, GENERAL_SETTINGS
    }

    public enum PetItemStyle implements ButtonCycling.SelectItem {
        NONE("settings.none"),
        DISPLAY_NAME("settings.petItemStyles.displayName"),
        SHOW_ITEM("settings.petItemStyles.showItem");

        private final String TRANSLATION_KEY;

        PetItemStyle(String translationKey) {
            this.TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public enum ChromaMode implements ButtonCycling.SelectItem {
        ALL_SAME_COLOR("settings.chromaModes.allTheSame"),
        FADE("settings.chromaModes.fade");

        private final String TRANSLATION_KEY;

        ChromaMode(String translationKey) {
            TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public enum AllFeaturesChroma implements ButtonCycling.SelectItem {
        DISABLED("messages.enableAll"),
        ENABLED("messages.disableAll");

        private final String TRANSLATION_KEY;

        AllFeaturesChroma(String translationKey) {
            TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return "";
        }
    }

    /**
     * Settings that modify the behavior of features- without technically being
     * a feature itself.
     * <p>
     * For the equivalent feature (that holds the state) use the ids instead of the enum directly
     * because the enum Feature depends on FeatureSetting, so FeatureSetting can't depend on Feature on creation.
     */
    public enum FeatureSetting {
        COLOR("settings.changeColor", -1),
        ENABLED_IN_OTHER_GAMES("settings.showInOtherGames", -1),
        REPEATING("settings.repeating", -1),
        TEXT_MODE("settings.textMode", -1),
        BACKPACK_STYLE("settings.backpackStyle", -1),
        ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", -1),
        DEPLOYABLE_DISPLAY_STYLE("settings.deployableDisplayStyle", -1),
        MAP_ZOOM("settings.mapZoom", -1),
        COLOUR_BY_RARITY("settings.colorByRarity", -1),
        PET_ITEM_STYLE("settings.petItemStyle", -1),

        DRAGONS_NEST_ONLY("settings.dragonsNestOnly", 128),
        USE_VANILLA_TEXTURE("settings.useVanillaTexture", 17),
        SHOW_ONLY_WHEN_HOLDING_SHIFT("settings.showOnlyWhenHoldingShift", 18),
        MAKE_INVENTORY_COLORED("settings.makeBackpackInventoriesColored", 43),
        CHANGE_BAR_COLOR_WITH_POTIONS("settings.changeBarColorForPotions", 46),
        CAKE_BAG_PREVIEW("settings.showCakeBagPreview", 71),
        ROTATE_MAP("settings.rotateMap", 100),
        CENTER_ROTATION_ON_PLAYER("settings.centerRotationOnYourPlayer", 101),
        SHOW_PLAYER_HEADS_ON_MAP("settings.showPlayerHeadsOnMap", 106),
        SHOW_GLOWING_ITEMS_ON_ISLAND("settings.showGlowingItemsOnIsland", 109),
        SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL("settings.skillActionsLeftUntilNextLevel", 115),
        HIDE_WHEN_NOT_IN_CRYPTS("settings.hideWhenNotDoingQuest", 133),
        HIDE_WHEN_NOT_IN_SPIDERS_DEN("settings.hideWhenNotDoingQuest", 134),
        HIDE_WHEN_NOT_IN_CASTLE("settings.hideWhenNotDoingQuest", 135),
        PERSONAL_COMPACTOR_PREVIEW("settings.showPersonalCompactorPreview", 137),
        SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP("settings.showSkillPercentageInstead", 144),
        SHOW_SKILL_XP_GAINED("settings.showSkillXPGained", 145),
        SHOW_SALVAGE_ESSENCES_COUNTER("settings.showSalvageEssencesCounter", 146),
        HEALING_CIRCLE_OPACITY("settings.healingCircleOpacity", 156),
        COOLDOWN_PREDICTION("settings.cooldownPrediction", 164),
        PERFECT_ENCHANT_COLOR("enchants.superTier", 165),
        GREAT_ENCHANT_COLOR("enchants.highTier", 166),
        GOOD_ENCHANT_COLOR("enchants.midTier", 167),
        POOR_ENCHANT_COLOR("enchants.lowTier", 168),
        COMMA_ENCHANT_COLOR("enchants.commas", 171),
        BIGGER_WAKE("settings.biggerWake", 170),
        HIGHLIGHT_ENCHANTMENTS("settings.highlightSpecialEnchantments", 153),
        HIDE_ENCHANTMENT_LORE("settings.hideEnchantDescription", 176),
        HIDE_GREY_ENCHANTS("settings.hideGreyEnchants", 87),
        ENCHANT_LAYOUT("enchantLayout.title", -1),
        TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR("settings.trevorTheTrapper.trackedEntityProximityIndicator", 173),
        TREVOR_HIGHLIGHT_TRACKED_ENTITY("settings.trevorTheTrapper.highlightTrackedEntity", 174),
        TREVOR_SHOW_QUEST_COOLDOWN("settings.trevorTheTrapper.showQuestCooldown", 175),
        SHOW_FETCHUR_ONLY_IN_DWARVENS("settings.showFetchurOnlyInDwarven", 179),
        SHOW_FETCHUR_ITEM_NAME("settings.showFetchurItemName", 180),
        SHOW_FETCHUR_INVENTORY_OPEN_ONLY("settings.showFetchurInventoryOpenOnly", 181),
        WARN_WHEN_FETCHUR_CHANGES("settings.warnWhenFetchurChanges", 182),
        STOP_ONLY_RAT_SQUEAK("settings.onlyStopRatSqueak", 184),
        ENDER_CHEST_PREVIEW("settings.showEnderChestPreview", 185),
        HIDE_WHEN_NOT_IN_END("settings.hideWhenNotDoingQuest", 187),
        HEALTH_PREDICTION("settings.vanillaHealthPrediction", 194),
        OUTLINE_DUNGEON_TEAMMATES("settings.outlineDungeonTeammates", 103),
        ITEM_GLOW("settings.glowingDroppedItems", 109),
        ABBREVIATE_SKILL_XP_DENOMINATOR("settings.abbreviateSkillXpDenominator", 198),
        OTHER_DEFENCE_STATS("settings.otherDefenseStats", 199),
        DISABLE_SPIRIT_SCEPTRE_MESSAGES("settings.disableDamageChatMessages", 203),
        OUTBID_ALERT("settings.outbidAlertSound", 206),
        DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME("settings.dontReplaceRomanNumeralsInItemNames", 210),
        RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU("settings.resetSalvagedEssencesAfterLeavingMenu", 214),
        CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD("settings.changeDungeonMapZoomWithKeyboard", 215),
        SHOW_PROFILE_TYPE( "settings.showProfileType", 219),
        SHOW_NETHER_FACTION("settings.showNetherFaction", 220),
		HIDE_WHEN_NOT_IN_CRIMSON("settings.hideWhenNotDoingQuest", 222),
        EXPAND_DEPLOYABLE_STATUS("settings.expandDeployableStatus", 226),
        TREVOR_BETTER_NAMETAG("settings.trevorTheTrapper.betterNametag", 227),
        HIDE_WHEN_NOT_IN_RIFT("settings.hideWhenNotDoingQuest", 231),
        ABBREVIATE_DRILL_FUEL_DENOMINATOR("settings.abbreviateDrillFuelDenominator", 232),
        SHOW_ONLY_HOLDING_FISHING_ROD("settings.showOnlyHoldingFishingRod", 233),
        HIDE_HEALTH_BAR_ON_RIFT("settings.hideHealthThingsOnRift", 234),
        HIDE_HEALTH_TEXT_ON_RIFT("settings.hideHealthThingsOnRift", 235),
        HIDE_HEALTH_UPDATES_ON_RIFT("settings.hideHealthThingsOnRift", 236),
        HIDE_ONLY_OUTSIDE_RIFT("settings.hideOnlyOutsideRift", 237),
        FIRE_FREEZE_SOUND("settings.fireFreezeSound", 239),
        FIRE_FREEZE_WHEN_HOLDING("settings.fireFreezeWhenHolding", 240),
        HEART_INSTEAD_HEALTH_ON_RIFT("settings.heartInsteadHealthOnRift", 244),
        OUTLINE_SHOWCASE_ITEMS("settings.outlineShowcaseItems", 245),
        BUILDERS_TOOL_PREVIEW("settings.showBuildersToolPreview", 248),
        HEALTH_TEXT_ICON("settings.healthTextIcon", 249),
        MANA_TEXT_ICON("settings.manaTextIcon", 250),
        DEFENCE_TEXT_ICON("settings.defenceTextIcon", 251),
        EFFECTIVE_HEALTH_TEXT_ICON("settings.effectiveHealthTextIcon", 252),
        ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR("settings.abbreviateThunderDisplaysDenominator", 253),
        CLASS_COLORED_TEAMMATE("settings.classColoredTeammate", 258),

        DISCORD_RP_STATE(0),
        DISCORD_RP_DETAILS(0),
        ;

        private final int FEATURE_EQUIVALENT;
        private final String TRANSLATION_KEY;

        FeatureSetting(int featureEquivalent) {
            this.TRANSLATION_KEY = null;
            FEATURE_EQUIVALENT = featureEquivalent;
        }

        FeatureSetting(String translationKey, int featureEquivalent) {
            this.TRANSLATION_KEY = translationKey;
            this.FEATURE_EQUIVALENT = featureEquivalent;
        }


        public Feature getFeatureEquivalent() {
            if (FEATURE_EQUIVALENT == -1) return null;

            for (Feature feature : Feature.values()) {
                if (feature.getId() == FEATURE_EQUIVALENT) {
                    return feature;
                }
            }
            return null;
        }

        public String getMessage(String... variables) {
            if (TRANSLATION_KEY != null) {
                return Translations.getMessage(TRANSLATION_KEY, (Object[]) variables);
            } else {
                return null;
            }
        }
    }

    public enum FeatureCredit {
        // If you make a feature, feel free to add your name here with an associated website of your choice.
        ORCHID_ALLOY("orchidalloy", "github.com/orchidalloy", Feature.SUMMONING_EYE_ALERT,
                Feature.FISHING_SOUND_INDICATOR, Feature.ENCHANTMENT_LORE_PARSING),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", Feature.PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", Feature.DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter", "github.com/tomocrafter", Feature.AVOID_BLINKING_NIGHT_VISION,
                Feature.SLAYER_ARMOR_PROGRESS, Feature.NO_ARROWS_LEFT_ALERT, Feature.BOSS_APPROACH_ALERT),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", Feature.MINION_DISABLE_LOCATION_WARNING),
        KEAGEL("Keagel", "github.com/Keagel", Feature.DISABLE_MAGICAL_SOUP_MESSAGES),
        SUPERHIZE("SuperHiZe", "github.com/superhize", Feature.SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER("DidiSkywalker", "twitter.com/didiskywalker", Feature.ITEM_PICKUP_LOG,
                Feature.HEALTH_UPDATES, Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS),
        P0KE("P0ke", "p0ke.dev", Feature.ZEALOT_COUNTER),
        DJTHEREDSTONER("DJtheRedstoner", "github.com/DJtheRedstoner", Feature.LEGENDARY_SEA_CREATURE_WARNING,
                Feature.HIDE_SVEN_PUP_NAMETAGS),
        CHARZARD("Charzard4261", "github.com/Charzard4261", Feature.DISABLE_TELEPORT_PAD_MESSAGES,
                Feature.BAIT_LIST, Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE, Feature.SHOW_ITEM_DUNGEON_FLOOR,
                Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE, Feature.REVENANT_SLAYER_TRACKER, Feature.TARANTULA_SLAYER_TRACKER,
                Feature.SVEN_SLAYER_TRACKER, Feature.DRAGON_STATS_TRACKER, Feature.PERSONAL_COMPACTOR_PREVIEW,
                Feature.SHOW_STACKING_ENCHANT_PROGRESS, Feature.STOP_BONZO_STAFF_SOUNDS, Feature.DISABLE_MORT_MESSAGES,
                Feature.DISABLE_BOSS_MESSAGES),
        IHDEVELOPER("iHDeveloper", "github.com/iHDeveloper", Feature.SHOW_DUNGEON_MILESTONE,
                Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY, Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY,
                Feature.DUNGEONS_SECRETS_DISPLAY, Feature.SHOW_SALVAGE_ESSENCES_COUNTER, Feature.SHOW_SWORD_KILLS,
                Feature.ENCHANTMENTS_HIGHLIGHT),
        TIRELESS_TRAVELER("TirelessTraveler", "github.com/ILikePlayingGames", Feature.DUNGEON_DEATH_COUNTER),
        KAASBROODJU("kaasbroodju", "github.com/kaasbroodju", Feature.SKILL_PROGRESS_BAR,
                Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP, Feature.SHOW_SKILL_XP_GAINED),
        PHOUBE("Phoube", "github.com/Phoube", Feature.HIDE_OTHER_PLAYERS_PRESENTS,
                Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS, Feature.DRILL_FUEL_BAR, Feature.DRILL_FUEL_TEXT,
                Feature.FISHING_PARTICLE_OVERLAY, Feature.BIGGER_WAKE, Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY,
                Feature.TREVOR_SHOW_QUEST_COOLDOWN/*, Feature.ONLY_MINE_ORES_DWARVEN_MINES, Feature.COOLDOWN_PREDICTION,
                Feature.ONLY_MINE_ORES_DWARVEN_MINES*/),
        PEDRO9558("Pedro9558", "github.com/Pedro9558", Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR,
                Feature.TREVOR_THE_TRAPPER_FEATURES, Feature.FETCHUR_TODAY, Feature.STOP_RAT_SOUNDS),
        ROBOTHANZO("RobotHanzo", "robothanzo.dev", Feature.HIDE_SPAWN_POINT_PLAYERS,
                Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES),
        IRONM00N("IRONM00N", "github.com/IRONM00N", Feature.FARM_EVENT_TIMER),
        SKYCATMINEPOKIE("skycatminepokie", "github.com/skycatminepokie", Feature.OUTBID_ALERT_SOUND),
        TIMOLOB("TimoLob", "github.com/TimoLob", Feature.BROOD_MOTHER_ALERT),
        NOPOTHEGAMER("NopoTheGamer", "twitch.tv/nopothegamer", Feature.BAL_BOSS_ALERT),
        CATFACE("CatFace","github.com/CattoFace",Feature.PLAYER_SYMBOLS_IN_CHAT),
        HANNIBAL2("Hannibal2", "github.com/hannibal00212", Feature.CRIMSON_ARMOR_ABILITY_STACKS,
                Feature.HIDE_TRUE_DEFENSE),
        JASON54("jason54jg", "github.com/jason54jg", Feature.INFERNO_SLAYER_TRACKER, Feature.INFERNO_COLOR_BY_RARITY,
                Feature.INFERNO_TEXT_MODE),
        GLACIALVITALITY("GlacialVitality", "github.com/glacialvitality", Feature.RIFTSTALKER_SLAYER_TRACKER,
                Feature.RIFTSTALKER_COLOR_BY_RARITY, Feature.RIFTSTALKER_TEXT_MODE),
        FIX3DLL("Fix3dll", "github.com/Fix3dll", Feature.FIRE_FREEZE_TIMER, Feature.HIDE_HAUNTED_SKULLS,
                Feature.THUNDER_BOTTLE_DISPLAY, Feature.PET_DISPLAY);

        private final Set<Feature> features;
        private final String author;
        private final String url;

        FeatureCredit(String author, String url, Feature... features) {
            this.features = EnumSet.of(features[0], features);
            this.author = author;
            this.url = url;
        }

        public static FeatureCredit fromFeature(Feature feature) {
            for (FeatureCredit credit : values()) {
                if (credit.features.contains(feature)) return credit;
            }
            return null;
        }

        public String getAuthor() {
            return "Contrib. " + author;
        }

        public String getUrl() {
            return "https://" + url;
        }
    }

    public enum DrawType {
        SKELETON_BAR,
        BAR,
        TEXT,
        PICKUP_LOG,
        DEFENCE_ICON,
        SLAYER_ARMOR_PROGRESS,
        DEPLOYABLE_DISPLAY,
        TICKER,
        BAIT_LIST_DISPLAY,
        DUNGEONS_MAP,
        SLAYER_TRACKERS,
        DRAGON_STATS_TRACKER,
        PROXIMITY_INDICATOR,
        PET_DISPLAY
    }

    @Getter
    public enum Social {
        GITHUB("github", "https://github.com/Fix3dll/SkyblockAddons"),
        MODRINTH("modrinth", "https://modrinth.com/mod/skyblockaddons-unofficial"),
        BUYMEACOFFEE("buymeacoffee", "https://www.buymeacoffee.com/fix3dll");

        private final ResourceLocation resourceLocation;
        private URI url;

        Social(String resourcePath, String url) {
            this.resourceLocation = new ResourceLocation("skyblockaddons", "gui/" + resourcePath + ".png");
            try {
                this.url = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS,
        SETTINGS,
        WARP
    }

    @Getter
    public enum DiscordStatusEntry {
        DETAILS(0),
        STATE(1);

        private final int id;

        DiscordStatusEntry(int id) {
            this.id = id;
        }
    }

    //TODO Fix for Hypixel localization
    @Getter @AllArgsConstructor
    public enum SlayerQuest {
        REVENANT_HORROR("Revenant Horror", SlayerBoss.REVENANT),
        TARANTULA_BROODFATHER("Tarantula Broodfather", SlayerBoss.TARANTULA),
        SVEN_PACKMASTER("Sven Packmaster", SlayerBoss.SVEN),
        VOIDGLOOM_SERAPH("Voidgloom Seraph", SlayerBoss.VOIDGLOOM),
        INFERNO_DEMONLORD("Inferno Demonlord", SlayerBoss.INFERNO),
        RIFTSTALKER_BLOODFIEND("Riftstalker Bloodfiend", SlayerBoss.RIFTSTALKER);

        private final String scoreboardName;
        private final SlayerBoss boss;

        public static SlayerQuest fromName(String scoreboardName) {
            for (SlayerQuest slayerQuest : SlayerQuest.values()) {
                if (slayerQuest.scoreboardName.equals(scoreboardName)) {
                    return slayerQuest;
                }
            }

            return null;
        }
    }
}
