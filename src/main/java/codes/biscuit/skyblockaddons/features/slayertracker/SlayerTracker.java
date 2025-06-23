package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.core.Rune;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;

public class SlayerTracker {

    @Getter private static final SlayerTracker instance = new SlayerTracker();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public int getSlayerKills(SlayerBoss slayerBoss) {
        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
        return slayerTrackerData.getSlayerKills().getOrDefault(slayerBoss, 0);
    }

    public int getDropCount(SlayerDrop slayerDrop) {
        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
        return slayerTrackerData.getSlayerDropCounts().getOrDefault(slayerDrop, 0);
    }

    /**
     * Returns whether any slayer trackers are enabled
     *
     * @return {@code true} if at least one slayer tracker is enabled, {@code false} otherwise
     */
    public boolean isTrackerEnabled() {
        return Feature.REVENANT_SLAYER_TRACKER.isEnabled() ||
                Feature.TARANTULA_SLAYER_TRACKER.isEnabled() ||
                Feature.SVEN_SLAYER_TRACKER.isEnabled() ||
                Feature.VOIDGLOOM_SLAYER_TRACKER.isEnabled() ||
                Feature.INFERNO_SLAYER_TRACKER.isEnabled() ||
                Feature.RIFTSTALKER_SLAYER_TRACKER.isEnabled();
    }

    /**
     * Adds a kill to the slayer type
     */
    public void completedSlayer(String slayerTypeText) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(slayerTypeText);
        if (slayerBoss != null) {
            SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
            slayerTrackerData.getSlayerKills().put(slayerBoss, slayerTrackerData.getSlayerKills().getOrDefault(slayerBoss, 0) + 1);
            slayerTrackerData.setLastKilledBoss(slayerBoss);

            main.getPersistentValuesManager().saveValues();
        }
    }

    /**
     * Resets all stat of the given slayer type
     * @param slayerType slayerType
     */
    public void resetAllStats(String slayerType) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(slayerType);

        if (slayerBoss == null) {
            throw new IllegalArgumentException(getMessage("commandUsage.sba.slayer.invalidBoss", slayerType));
        }

        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();

        slayerTrackerData.getSlayerKills().put(slayerBoss, 0);

        for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
            slayerTrackerData.getSlayerDropCounts().put(slayerDrop, 0);
        }
        main.getPersistentValuesManager().saveValues();
        Utils.sendMessage(getMessage("commands.responses.sba.slayer.resetBossStats", slayerType));
    }

    /**
     * Sets the value of a specific slayer stat
     * <p>
     * This method is called from {@link codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand#processCommand(ICommandSender, String[])}
     * when the player runs the command to change the slayer tracker stats.
     *
     * @param args the arguments provided when the player executed the command
     */
    public void setStatManually(String[] args) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(args[1]);

        if (slayerBoss == null) {
            throw new IllegalArgumentException(getMessage("commandUsage.sba.slayer.invalidBoss", args[1]));
        }

        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
        if (args[2].equalsIgnoreCase("kills")) {
            int count = Integer.parseInt(args[3]);
            slayerTrackerData.getSlayerKills().put(slayerBoss, count);
            Utils.sendMessage(getMessage("commandUsage.sba.slayer.killsSet", args[1], args[3]));
            main.getPersistentValuesManager().saveValues();
            return;
        }

        SlayerDrop slayerDrop;
        try {
            slayerDrop = SlayerDrop.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ex) {
            slayerDrop = null;
        }

        if (slayerDrop != null) {
            int count = Integer.parseInt(args[3]);
            slayerTrackerData.getSlayerDropCounts().put(slayerDrop, count);
            Utils.sendMessage(getMessage(
                    "commandUsage.sba.slayer.statSet", args[2], args[1], args[3]));
            main.getPersistentValuesManager().saveValues();
            return;
        }

        throw new IllegalArgumentException(getMessage("commandUsage.sba.slayer.invalidStat", args[1]));
    }

    public void setKillCount(SlayerBoss slayerBoss, int kills) {
        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
        slayerTrackerData.getSlayerKills().put(slayerBoss, kills);
    }

    // TODO dont count dropped items by player again
    public void addToTrackerData(NBTTagCompound ea, int amount, EnumUtils.SlayerQuest activeQuest) {
        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();

        for (SlayerDrop drop : activeQuest.getBoss().getDrops()) {
            if (!drop.getSkyblockID().equals(ItemUtils.getSkyblockItemID(ea))) continue;

            // If this is a rune and it doesn't match, continue
            Rune rune = ItemUtils.getRuneData(ea);
            if (drop.getRuneID() != null && (rune == null || rune.getType() == null || !rune.getType().equals(drop.getRuneID()))) {
                continue;
            }
            // If this is a book and it doesn't match, continue
            if (drop.getSkyblockID().equals("ENCHANTED_BOOK")) {
                boolean match = true;
                NBTTagCompound diffTag = ea.getCompoundTag("enchantments");
                NBTTagCompound dropTag = ItemUtils.getEnchantments(drop.getItemStack());
                if (diffTag != null && dropTag != null && diffTag.getKeySet().size() == dropTag.getKeySet().size()) {
                    for (String key : diffTag.getKeySet()) {
                        if (!dropTag.hasKey(key, Constants.NBT.TAG_INT) || dropTag.getInteger(key) != diffTag.getInteger(key)) {
                            match = false;
                            break;
                        }
                    }
                } else {
                    match = false;
                }
                if (!match) {
                    continue;
                }
            }
            slayerTrackerData.getSlayerDropCounts().put(drop, slayerTrackerData.getSlayerDropCounts().getOrDefault(drop, 0) + amount);

            if (DevUtils.isLoggingSlayerTracker()) {
                Utils.sendMessage(String.format("§fx%d §%s%s"
                        , amount
                        , drop.getRarity().getColorCode().getCode()
                        , drop.getDisplayName()
                        ), true
                );
            }
        }
    }
}