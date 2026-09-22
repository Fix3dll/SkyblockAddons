package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerBoss;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Set;

import static com.fix3dll.skyblockaddons.core.feature.Feature.*;

public class EnumUtils {

    @SuppressWarnings("unused")
    @Getter
    public enum AnchorPoint implements RegistrableEnum {
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

        public static AnchorPoint fromId(int id) {
            for (AnchorPoint feature : values()) {
                if (feature.getId() == id) {
                    return feature;
                }
            }
            return null;
        }

        public int getX(int maxX) {
            return switch (this) {
                case TOP_RIGHT, BOTTOM_RIGHT -> maxX;
                case TOP_MIDDLE, BOTTOM_MIDDLE -> maxX / 2;
                default -> 0;
            };
        }

        public int getY(int maxY) {
            return switch (this) {
                case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_MIDDLE -> maxY;
                default -> 0;
            };
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

    public enum BackpackStyle implements ButtonCycling.SelectItem, RegistrableEnum {
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

    public enum DeployableDisplayStyle implements ButtonCycling.SelectItem, RegistrableEnum {
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

    public enum TextStyle implements ButtonCycling.SelectItem, RegistrableEnum {
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

    public enum PetItemStyle implements ButtonCycling.SelectItem, RegistrableEnum {
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

    public enum ChromaMode implements ButtonCycling.SelectItem, RegistrableEnum {
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

    public enum AutoUpdateMode implements ButtonCycling.SelectItem, RegistrableEnum {
        UPDATE_OFF("settings.autoUpdate.disabled.title", "settings.autoUpdate.disabled.description"),
        STABLE("settings.autoUpdate.stable.title", "settings.autoUpdate.stable.description"),
        LATEST("settings.autoUpdate.latest.title", "settings.autoUpdate.latest.description");

        private final String TRANSLATION_KEY;
        private final String DESCRIPTION_KEY;

        AutoUpdateMode(String translationKey, String descriptionKey) {
            TRANSLATION_KEY = translationKey;
            DESCRIPTION_KEY = descriptionKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            if (DESCRIPTION_KEY != null) {
                return Translations.getMessage(DESCRIPTION_KEY);
            }
            return null;
        }
    }

    public enum LBinAveragesType implements ButtonCycling.SelectItem, RegistrableEnum {
        ONE_DAY("settings.itemPricesInTooltip.avgTypes.1day", "1day.gz"),
        THREE_DAY("settings.itemPricesInTooltip.avgTypes.3day", "3day.gz"),
        SEVEN_DAY("settings.itemPricesInTooltip.avgTypes.7day", "7day.gz");

        private final String TRANSLATION_KEY;
        @Getter private final String urlPath;

        LBinAveragesType(String translationKey, String urlPath) {
            TRANSLATION_KEY = translationKey;
            this.urlPath = urlPath;
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

    /** If you make a feature, feel free to add your GitHub username here. */
    @NullMarked
    public enum FeatureCredit {
        ORCHID_ALLOY("orchidalloy", SUMMONING_EYE_ALERT, ENCHANTMENT_LORE_PARSING),
        HIGH_CRIT("HighCrit", PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter", SLAYER_ARMOR_PROGRESS, NO_ARROWS_LEFT_ALERT, BOSS_APPROACH_ALERT),
        DAPIGGUY("DaPigGuy", MINION_DISABLE_LOCATION_WARNING),
        KEAGEL("Keagel", DISABLE_MAGICAL_SOUP_MESSAGES),
        SUPERHIZE("SuperHiZe", SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER("DidiSkywalker", ITEM_PICKUP_LOG, HEALTH_UPDATES, REPLACE_ROMAN_NUMERALS_WITH_NUMBERS),
        P0KE("P0keDev", ZEALOT_COUNTER),
        DJTHEREDSTONER("DJtheRedstoner", LEGENDARY_SEA_CREATURE_WARNING, HIDE_SVEN_PUP_NAMETAGS),
        CHARZARD("Charzard4261", DISABLE_TELEPORT_PAD_MESSAGES, SHOW_BASE_STAT_BOOST_PERCENTAGE, DISABLE_BOSS_MESSAGES,
                SHOW_ITEM_DUNGEON_FLOOR, SHOW_BASE_STAT_BOOST_PERCENTAGE, REVENANT_SLAYER_TRACKER,
                TARANTULA_SLAYER_TRACKER, SVEN_SLAYER_TRACKER, DRAGON_STATS_TRACKER, SHOW_STACKING_ENCHANT_PROGRESS,
                STOP_BONZO_STAFF_SOUNDS, DISABLE_MORT_MESSAGES),
        IHDEVELOPER("iHDeveloper", SHOW_DUNGEON_MILESTONE, DUNGEONS_COLLECTED_ESSENCES_DISPLAY, SHOW_SWORD_KILLS,
                SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY, DUNGEONS_SECRETS_DISPLAY),
        TIRELESS_TRAVELER("ILikePlayingGames", DUNGEON_DEATH_COUNTER),
        KAASBROODJU("kaasbroodju", SKILL_PROGRESS_BAR),
        PHOUBE("Phoube", HIDE_OTHER_PLAYERS_PRESENTS, SHOW_EXPERIMENTATION_TABLE_TOOLTIPS, DRILL_FUEL_BAR,
                DRILL_FUEL_TEXT),
        PEDRO9558("Pedro9558", TREVOR_THE_TRAPPER_FEATURES, FETCHUR_TODAY, STOP_RAT_SOUNDS),
        ROBOTHANZO("RobotHanzo", HIDE_SPAWN_POINT_PLAYERS),
        IRONM00N("IRONM00N", FARM_EVENT_TIMER),
        SKYCATMINEPOKIE("skycatminepokie", OUTBID_ALERT_SOUND),
        TIMOLOB("TimoLob", BROOD_MOTHER_ALERT),
        NOPOTHEGAMER("NopoTheGamer", BAL_BOSS_ALERT),
        CATFACE("CattoFace", PLAYER_SYMBOLS_IN_CHAT),
        HANNIBAL2("hannibal002", CRIMSON_ARMOR_ABILITY_STACKS, HIDE_TRUE_DEFENSE),
        JASON54("Jason54jg", INFERNO_SLAYER_TRACKER),
        GLACIALVITALITY("GlacialVitality", RIFTSTALKER_SLAYER_TRACKER),
        FIX3DLL("Fix3dll", FIRE_FREEZE_TIMER, HIDE_HAUNTED_SKULLS, THUNDER_BOTTLE_DISPLAY, PET_DISPLAY, PRESSURE_BAR,
                PRESSURE_TEXT, HIDE_EFFECTS_HUD, EQUIPMENTS_IN_INVENTORY, COLORED_FISHING_PARTICLES, VITALITY_TEXT,
                ITEM_PRICES_IN_TOOLTIP, SHOW_CLICKABLE_MESSAGES_CONTENT, DUNGEON_PROFIT_OVERLAY, VITALITY_BAR);

        private final Set<Feature> features;
        private final String author;
        @Getter private final URI uri;

        FeatureCredit(String githubUsername, Feature... features) {
            this.features = EnumSet.of(features[0], features);
            this.author = githubUsername;
            try {
                this.uri = new URI("https", "github.com", "/" + githubUsername, null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid github username", e);
            }
        }

        public static @Nullable FeatureCredit fromFeature(Feature feature) {
            for (FeatureCredit credit : values()) {
                if (credit.features.contains(feature)) return credit;
            }
            return null;
        }

        public String getAuthor() {
            return "Contrib. " + author;
        }
    }

    public enum DrawType {
        SKELETON_BAR,
        BAR,
        TEXT,
        PICKUP_LOG,
        SLAYER_ARMOR_PROGRESS,
        DEPLOYABLE_DISPLAY,
        TICKER,
        DUNGEONS_MAP,
        SLAYER_TRACKERS,
        DRAGON_STATS_TRACKER,
        PROXIMITY_INDICATOR,
        PET_DISPLAY
    }

    @Getter
    @NullMarked
    public enum Social {
        GITHUB("github", "https://github.com/Fix3dll/SkyblockAddons"),
        MODRINTH("modrinth", "https://modrinth.com/project/F35D4vTL"),
        BUYMEACOFFEE("buymeacoffee", "https://www.buymeacoffee.com/fix3dll");

        private final Identifier identifier;
        private final URI uri;

        Social(String resourcePath, String url) {
            this.identifier = SkyblockAddons.identifier("gui/" + resourcePath + ".png");
            try {
                this.uri = new URI(url);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid %s social url".formatted(this.name()), e);
            }
        }
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS,
        SETTINGS,
        WARP
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