package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerBoss;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

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

    public enum FeatureCredit {
        // If you make a feature, feel free to add your name here with an associated website of your choice.
        ORCHID_ALLOY("orchidalloy", "github.com/orchidalloy", SUMMONING_EYE_ALERT, FISHING_SOUND_INDICATOR,
                ENCHANTMENT_LORE_PARSING),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter", "github.com/tomocrafter", AVOID_BLINKING_NIGHT_VISION, SLAYER_ARMOR_PROGRESS,
                NO_ARROWS_LEFT_ALERT, BOSS_APPROACH_ALERT),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", MINION_DISABLE_LOCATION_WARNING),
        KEAGEL("Keagel", "github.com/Keagel", DISABLE_MAGICAL_SOUP_MESSAGES),
        SUPERHIZE("SuperHiZe", "github.com/superhize", SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER("DidiSkywalker", "twitter.com/didiskywalker", ITEM_PICKUP_LOG, HEALTH_UPDATES,
                REPLACE_ROMAN_NUMERALS_WITH_NUMBERS),
        P0KE("P0ke", "p0ke.dev", ZEALOT_COUNTER),
        DJTHEREDSTONER("DJtheRedstoner", "github.com/DJtheRedstoner", LEGENDARY_SEA_CREATURE_WARNING,
                HIDE_SVEN_PUP_NAMETAGS),
        CHARZARD("Charzard4261", "github.com/Charzard4261", DISABLE_TELEPORT_PAD_MESSAGES, BAIT_LIST,
                SHOW_BASE_STAT_BOOST_PERCENTAGE, SHOW_ITEM_DUNGEON_FLOOR, SHOW_BASE_STAT_BOOST_PERCENTAGE,
                REVENANT_SLAYER_TRACKER, TARANTULA_SLAYER_TRACKER, SVEN_SLAYER_TRACKER, DRAGON_STATS_TRACKER,
                SHOW_STACKING_ENCHANT_PROGRESS, STOP_BONZO_STAFF_SOUNDS, DISABLE_MORT_MESSAGES, DISABLE_BOSS_MESSAGES),
        IHDEVELOPER("iHDeveloper", "github.com/iHDeveloper", SHOW_DUNGEON_MILESTONE,
                DUNGEONS_COLLECTED_ESSENCES_DISPLAY, SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY, DUNGEONS_SECRETS_DISPLAY,
                SHOW_SWORD_KILLS),
        TIRELESS_TRAVELER("TirelessTraveler", "github.com/ILikePlayingGames", DUNGEON_DEATH_COUNTER),
        KAASBROODJU("kaasbroodju", "github.com/kaasbroodju", SKILL_PROGRESS_BAR),
        PHOUBE("Phoube", "github.com/Phoube", HIDE_OTHER_PLAYERS_PRESENTS, SHOW_EXPERIMENTATION_TABLE_TOOLTIPS,
                DRILL_FUEL_BAR, DRILL_FUEL_TEXT, FISHING_PARTICLE_OVERLAY),
        PEDRO9558("Pedro9558", "github.com/Pedro9558", TREVOR_THE_TRAPPER_FEATURES, FETCHUR_TODAY, STOP_RAT_SOUNDS),
        ROBOTHANZO("RobotHanzo", "robothanzo.dev", HIDE_SPAWN_POINT_PLAYERS),
        IRONM00N("IRONM00N", "github.com/IRONM00N", FARM_EVENT_TIMER),
        SKYCATMINEPOKIE("skycatminepokie", "github.com/skycatminepokie", OUTBID_ALERT_SOUND),
        TIMOLOB("TimoLob", "github.com/TimoLob", BROOD_MOTHER_ALERT),
        NOPOTHEGAMER("NopoTheGamer", "twitch.tv/nopothegamer", BAL_BOSS_ALERT),
        CATFACE("CatFace","github.com/CattoFace", PLAYER_SYMBOLS_IN_CHAT),
        HANNIBAL2("Hannibal2", "github.com/hannibal00212", CRIMSON_ARMOR_ABILITY_STACKS, HIDE_TRUE_DEFENSE),
        JASON54("jason54jg", "github.com/jason54jg", INFERNO_SLAYER_TRACKER),
        GLACIALVITALITY("GlacialVitality", "github.com/glacialvitality", RIFTSTALKER_SLAYER_TRACKER),
        FIX3DLL("Fix3dll", "github.com/Fix3dll", FIRE_FREEZE_TIMER, HIDE_HAUNTED_SKULLS, THUNDER_BOTTLE_DISPLAY,
                PET_DISPLAY, PRESSURE_BAR, PRESSURE_TEXT);

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
            this.resourceLocation = SkyblockAddons.resourceLocation("gui/" + resourcePath + ".png");
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
