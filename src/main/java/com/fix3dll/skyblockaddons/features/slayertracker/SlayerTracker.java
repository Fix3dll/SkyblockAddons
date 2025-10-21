package com.fix3dll.skyblockaddons.features.slayertracker;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.commands.SkyblockAddonsCommand;
import com.fix3dll.skyblockaddons.core.SkyblockRune;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;

import java.util.Locale;
import java.util.Map;

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
            throw new IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidBoss", slayerType));
        }

        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();

        slayerTrackerData.getSlayerKills().put(slayerBoss, 0);

        for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
            slayerTrackerData.getSlayerDropCounts().put(slayerDrop, 0);
        }
        main.getPersistentValuesManager().saveValues();
        Utils.sendMessage(Translations.getMessage("commands.responses.sba.slayer.resetBossStats", slayerType));
    }

    /**
     * Sets the value of a specific slayer stat.
     * @see SkyblockAddonsCommand#buildCommands()
     */
    public void setStatManually(String boss, String stat, int value) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(boss);

        if (slayerBoss == null) {
            throw new IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidBoss", boss));
        }

        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();
        if ("kills".equalsIgnoreCase(stat)) {
            slayerTrackerData.getSlayerKills().put(slayerBoss, value);
            Utils.sendMessage(Translations.getMessage("commandUsage.sba.slayer.killsSet", boss, value));
            main.getPersistentValuesManager().saveValues();
            return;
        }

        SlayerDrop slayerDrop;
        try {
            slayerDrop = SlayerDrop.valueOf(stat.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            slayerDrop = null;
        }

        if (slayerDrop != null) {
            slayerTrackerData.getSlayerDropCounts().put(slayerDrop, value);
            Utils.sendMessage(Translations.getMessage("commandUsage.sba.slayer.statSet", stat, boss, value));
            main.getPersistentValuesManager().saveValues();
            return;
        }

        throw new IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidStat", boss));
    }

    // TODO dont count dropped items by player again
    public void addToTrackerData(CompoundTag ea, int amount, EnumUtils.SlayerQuest activeQuest) {
        SlayerTrackerData slayerTrackerData = main.getPersistentValuesManager().getPersistentValues().getSlayerTracker();

        for (SlayerDrop drop : activeQuest.getBoss().getDrops()) {
            if (!drop.getSkyblockID().equals(ItemUtils.getSkyblockItemID(ea))) continue;

            // If this is a rune, and it doesn't match, continue
            if (drop.getRuneID() != null) {
                SkyblockRune rune = ItemUtils.getRuneData(ea);
                if (rune == null || rune.getType() == null || !rune.getType().equals(drop.getRuneID())) {
                    continue;
                }
            }
            // If this is a attribute shard, and it doesn't match, continue
            if (drop.getAttributeNbtKey() != null && !ItemUtils.getAttributes(ea).containsKey(drop.getAttributeNbtKey())) {
                continue;
            }
            // If this is a book and it doesn't match, continue
            if (drop.getSkyblockID().equals("ENCHANTED_BOOK")) {
                Map<String, Integer> diffTag = ItemUtils.getEnchantments(ea);
                Map<String, Integer> dropTag = ItemUtils.getEnchantments(drop.getItemStack());
                if (!dropTag.equals(diffTag)) {
                    continue;
                }
            }
            slayerTrackerData.getSlayerDropCounts().put(drop, slayerTrackerData.getSlayerDropCounts().getOrDefault(drop, 0) + amount);

            if (DevUtils.isLoggingSlayerTracker()) {
                Utils.sendMessage(
                        String.format("§fx%d §%s%s", amount, drop.getRarity().getColorCode().getCode(), drop.getDisplayName()),
                        true
                );
            }
        }
    }

}