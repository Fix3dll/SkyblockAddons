package codes.biscuit.skyblockaddons.features.discordrpc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Statuses that are shown on the Discord RPC feature
 * This file has LF line endings because ForgeGradle is weird and will throw a NullPointerException if it's CRLF.
 */
public enum DiscordStatus implements ButtonSelect.SelectItem {

    NONE("discordStatus.titleNone", "discordStatus.descriptionNone", () -> null),
    LOCATION("discordStatus.titleLocation", "discordStatus.descriptionLocation",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                Location location = main.getUtils().getLocation();

                // Don't display "Your Island."
                if (location.equals(Location.ISLAND)) {
                    return "\u23E3 ".concat("Private Island");
                } else if (location.equals(Location.THE_CATACOMBS)) {
                    return "\u23E3 ".concat(Location.THE_CATACOMBS.getScoreboardName())
                            .concat(main.getUtils().getDungeonFloor());
                } else if (location.equals(Location.KUUDRAS_HOLLOW)) {
                    return "\u23E3 ".concat(Location.KUUDRAS_HOLLOW.getScoreboardName())
                            .concat(main.getUtils().getDungeonFloor());
                } else {
                    return SkyblockAddons.getInstance().getUtils().isOnRift()
                            ? "\u0444 ".concat(location.getScoreboardName())
                            : "\u23E3 ".concat(location.getScoreboardName());
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
                final Map<Attribute, MutableFloat> attributes = SkyblockAddons.getInstance().getUtils().getAttributes();

                String health = TextUtils.formatNumber(attributes.get(Attribute.HEALTH).getValue());
                String defense = TextUtils.formatNumber(attributes.get(Attribute.DEFENCE).getValue());
                String mana = TextUtils.formatNumber(attributes.get(Attribute.MANA).getValue());

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
                Location location = main.getUtils().getLocation();

                if (location == Location.THE_END || location == Location.DRAGONS_NEST) {
                    return DiscordStatus.ZEALOTS.displayMessageSupplier.get();
                }

                EnumUtils.SlayerQuest slayerQuest = main.getUtils().getSlayerQuest();
                if (slayerQuest != null && LocationUtils.isSlayerLocation(slayerQuest, location)) {
                    return (main.getUtils().isSlayerBossAlive() ? "Slaying a " : "Doing a ")
                            + slayerQuest.getScoreboardName() + " " + main.getUtils().getSlayerQuestLevel() + " boss.";
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
        this.title = Translations.getMessage(titleTranslationKey);
        this.description = Translations.getMessage(descriptionTranslationKey);
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString(EnumUtils.DiscordStatusEntry currentEntry) {
        SkyblockAddons.getInstance().getDiscordRPCManager().setCurrentEntry(currentEntry);
        return displayMessageSupplier.get();
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }
}