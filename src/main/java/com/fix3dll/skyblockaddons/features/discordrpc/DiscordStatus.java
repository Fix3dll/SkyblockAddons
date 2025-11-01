package com.fix3dll.skyblockaddons.features.discordrpc;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.PlayerStat;
import com.fix3dll.skyblockaddons.core.SkyblockDate;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Statuses that are shown on the Discord RPC feature
 */
@SuppressWarnings("UnnecessaryUnicodeEscape")
public enum DiscordStatus implements ButtonCycling.SelectItem, RegistrableEnum {

    NONE("discordStatus.titleNone", "discordStatus.descriptionNone", () -> null),
    LOCATION("discordStatus.titleLocation", "discordStatus.descriptionLocation",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();

                String location = main.getUtils().getLocation();
                Island map = main.getUtils().getMap();
                String prefix = main.getUtils().isOnRift() ? "ф " : "⏣ ";

                switch (map) {
                    // Don't display "Your Island"
                    case PRIVATE_ISLAND:
                        if (main.getUtils().isGuest()) {
                            return "Visiting " + location.trim();
                        } else {
                            return "⏣ Private Island";
                        }
                    case GARDEN:
                        // If the title line ends with "GUEST", then the player is visiting someone else's island.
                        if (main.getUtils().isGuest()) {
                            return "Visiting The Garden";
                        } else {
                            String display = prefix + location;
                            String plotName = main.getUtils().getPlotName();
                            if (!plotName.isEmpty()) {
                                display += " - " + plotName;
                            }
                            return display;
                        }
                    default:
                        return prefix + location;
                }
            }),

    PURSE("discordStatus.titlePurse", "discordStatus.descriptionPurse",
            () -> {
                double coins = SkyblockAddons.getInstance().getUtils().getPurse();

                return TextUtils.formatNumber(coins) + " Coin" + (coins == 1 ? "" : "s");
            }),

    BITS("discordStatus.titleBits", "discordStatus.descriptionBits",
            ()-> {
                double bits = SkyblockAddons.getInstance().getUtils().getBits();


                return TextUtils.formatNumber(bits) + " Bit" + (bits == 1 ? "" : "s");
            }),

    MOTES("discordStatus.titleMotes", "discordStatus.descriptionMotes",
            ()-> {
                double motes = SkyblockAddons.getInstance().getUtils().getMotes();

                return TextUtils.formatNumber(motes) + " Mote" + (motes == 1 ? "" : "s");
            }),

    STATS("discordStatus.titleStats", "discordStatus.descriptionStats",
            () -> {
                String health = TextUtils.formatNumber(PlayerStat.HEALTH.getValue());
                String defense = TextUtils.formatNumber(PlayerStat.DEFENCE.getValue());
                String mana = TextUtils.formatNumber(PlayerStat.MANA.getValue());

                return String.format("%s\u2764 %s\u2748 %s\u270E", health, defense, mana);
            }),

    ZEALOTS("discordStatus.titleZealots", "discordStatus.descriptionZealots",
            () -> String.format(
                    "%d Zealots killed"
                    , SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getKills())
            ),

    ITEM("discordStatus.titleItem", "discordStatus.descriptionItem",
            () -> {
                final LocalPlayer player = Minecraft.getInstance().player;
                if(player != null && player.getMainHandItem() != ItemStack.EMPTY) {
                    Component itemName = player.getMainHandItem().getCustomName();
                    if (itemName != null) {
                        return String.format("Holding %s", itemName.getString());
                    }
                }
                return "No item in hand";
            }),

    TIME("discordStatus.titleTime", "discordStatus.descriptionTime",
            () -> {
                final SkyblockDate date = SkyblockAddons.getInstance().getUtils().getCurrentDate();
                return date != null ? date.toString() : "";
            }),

    PROFILE("discordStatus.titleProfile", "discordStatus.descriptionProfile",
            () -> {
                String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
                return String.format("Profile: %s", profile == null ? "None" : profile);
            }),

    CUSTOM("discordStatus.titleCustom", "discordStatus.descriptionCustom",
            () -> {
                FeatureSetting currentStatus = SkyblockAddons.getInstance().getDiscordRPCManager().getCurrentStatus();

                String text;
                if (currentStatus != null) {
                    text = Feature.DISCORD_RPC.getAsString(currentStatus);
                } else {
                    return "!!";
                }
                return text.substring(0, Math.min(text.length(), 100));
            }),

    AUTO_STATUS("discordStatus.titleAuto", "discordStatus.descriptionAuto", () -> {
        SkyblockAddons main = SkyblockAddons.getInstance();

        EnumUtils.SlayerQuest slayerQuest = main.getUtils().getSlayerQuest();
        if (slayerQuest != null && LocationUtils.isOnSlayerLocation(slayerQuest)) {
            return (main.getUtils().isSlayerBossAlive() ? "Slaying a " : "Doing a ")
                    + slayerQuest.getScoreboardName() + " " + main.getUtils().getSlayerQuestLevel() + " boss.";
        }

        if (LocationUtils.isOnZealotSpawnLocation()) {
            return DiscordStatus.ZEALOTS.displayMessageSupplier.get();
        }

        if (main.getUtils().isOnRift()) {
            return DiscordStatus.valueOf("MOTES").displayMessageSupplier.get();
        }
        Feature feature = Feature.DISCORD_RPC;

        // Avoid self reference.
        if ("AUTO_STATUS".equals(feature.getAsEnum(FeatureSetting.DISCORD_RP_AUTO_MODE).name())) {
            feature.set(FeatureSetting.DISCORD_RP_AUTO_MODE, DiscordStatus.NONE);
        }

        DiscordStatus mode = (DiscordStatus) feature.getAsEnum(FeatureSetting.DISCORD_RP_AUTO_MODE);
        return mode.displayMessageSupplier.get();
    })
    ;

    private final String title;
    private final String description;
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(String titleTranslationKey, String descriptionTranslationKey, Supplier<String> displayMessageSupplier) {
        this.title = titleTranslationKey;
        this.description = descriptionTranslationKey;
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString(FeatureSetting currentStatus) {
        SkyblockAddons.getInstance().getDiscordRPCManager().setCurrentStatus(currentStatus);
        return displayMessageSupplier.get();
    }

    @Override
    public String getDisplayName() {
        return Translations.getMessage(title);
    }

    @Override
    public String getDescription() {
        return Translations.getMessage(description);
    }
}