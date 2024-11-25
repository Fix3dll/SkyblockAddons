package codes.biscuit.skyblockaddons.features.discordrpc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.function.Supplier;

/**
 * Statuses that are shown on the Discord RPC feature
 */
@SuppressWarnings("UnnecessaryUnicodeEscape")
public enum DiscordStatus implements ButtonSelect.SelectItem {

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

                if (coins == 1)
                    return TextUtils.formatNumber(coins) + " Coin";
                else
                    return TextUtils.formatNumber(coins) + " Coins";
            }),

    BITS("discordStatus.titleBits", "discordStatus.descriptionBits",
            ()-> {
                double bits = SkyblockAddons.getInstance().getUtils().getBits();

                if (bits == 1)
                    return TextUtils.formatNumber(bits) + " Bit";
                else
                    return TextUtils.formatNumber(bits) + " Bits";
            }),

    MOTES("discordStatus.titleMotes", "discordStatus.descriptionMotes",
            ()-> {
                double motes = SkyblockAddons.getInstance().getUtils().getMotes();

                if (motes == 1)
                    return TextUtils.formatNumber(motes) + " Mote";
                else
                    return TextUtils.formatNumber(motes) + " Motes";
            }),

    STATS("discordStatus.titleStats", "discordStatus.descriptionStats",
            () -> {
                String health = TextUtils.formatNumber(PlayerStats.HEALTH.getValue());
                String defense = TextUtils.formatNumber(PlayerStats.DEFENCE.getValue());
                String mana = TextUtils.formatNumber(PlayerStats.MANA.getValue());

                return String.format("%s\u2764 %s\u2748 %s\u270E", health, defense, mana);
            }),

    ZEALOTS("discordStatus.titleZealots", "discordStatus.descriptionZealots",
            () -> String.format(
                    "%d Zealots killed"
                    , SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getKills())
            ),

    ITEM("discordStatus.titleItem", "discordStatus.descriptionItem",
            () -> {
                final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if(player != null && player.getHeldItem() != null) {
                    return String.format("Holding %s", TextUtils.stripColor(player.getHeldItem().getDisplayName()));
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
                SkyblockAddons main = SkyblockAddons.getInstance();

                String text = main.getConfigValues().getCustomStatus(main.getDiscordRPCManager().getCurrentEntry());
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

                if ("AUTO_STATUS".equals(main.getConfigValues().getDiscordAutoDefault().name())) { // Avoid self reference.
                    main.getConfigValues().setDiscordAutoDefault(DiscordStatus.NONE);
                }

                return main.getConfigValues().getDiscordAutoDefault().displayMessageSupplier.get();
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

    public String getDisplayString(EnumUtils.DiscordStatusEntry currentEntry) {
        SkyblockAddons.getInstance().getDiscordRPCManager().setCurrentEntry(currentEntry);
        return displayMessageSupplier.get();
    }

    @Override
    public String getName() {
        return Translations.getMessage(title);
    }

    @Override
    public String getDescription() {
        return Translations.getMessage(description);
    }
}