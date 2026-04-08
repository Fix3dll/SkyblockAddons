package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.FetchurManager;
import com.fix3dll.skyblockaddons.features.backpacks.CompressedStorage;
import com.fix3dll.skyblockaddons.features.dragontracker.DragonTrackerData;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTrackerData;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PersistentValuesManager extends AbstractPersistentDataManager<PersistentValuesManager.PersistentValues> {

    private final File configDir;

    @Getter @Setter
    public static class PersistentValues {
        private int kills = 0; // Kills since last eye
        private int totalKills = 0; // Lifetime zealots killed
        private int summoningEyeCount = 0; // Lifetime summoning eyes

        private SlayerTrackerData slayerTracker = new SlayerTrackerData();
        private DragonTrackerData dragonTracker = new DragonTrackerData();

        private Map<String, CompressedStorage> storageCache = new HashMap<>();
        private Map<String, Set<Integer>> profileLockedSlots = new HashMap<>();

        private int oresMined = 0;
        private int seaCreaturesKilled = 0;

        private long lastTimeFetchur = 0L; // Last time the player gave Fetchur the correct item in ms from epoch

//        private HypixelLanguage hypixelLanguage = HypixelLanguage.ENGLISH;
    }

    public PersistentValuesManager(File mainConfigDir) {
        super(mainConfigDir, "persistentValues.json", PersistentValues.class);
        this.configDir = mainConfigDir;
    }

    @Override
    protected PersistentValues createDefault() {
        return new PersistentValues();
    }

    @Override
    protected void onPostLoad() {
        FetchurManager.getInstance().postPersistentConfigLoad(data.getLastTimeFetchur());
    }

    @Override
    protected void handleLoadError() {
        backupValues();
    }

    /**
     * Creates a backup of the corrupted 'persistentValues.json' file before it gets overwritten.
     */
    public void backupValues() {
        if (!dataFile.exists()) {
            logger.warn("persistentValues.json file for backup does not exist!");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            String formattedDate = ZonedDateTime.now().format(formatter);
            String backupFileName = "persistentValues.json." + formattedDate + ".backup";

            File backupFile = new File(configDir, "/skyblockaddons/backup/" + backupFileName);
            Files.createDirectories(backupFile.getParentFile().toPath());

            Files.copy(dataFile.toPath(), backupFile.toPath());
            logger.info("Persistent values backed up successfully: {}", backupFile.getPath());
        } catch (IOException e) {
            logger.error("Failed to backup persistent values file!", e);
        }
    }

    /**
     * Adds one to the summoning eye counter, adds the kills since last eye to the lifetime kill counter,
     * and resets the kills since last eye counter.
     */
    public void addEyeResetKills() {
        data.setSummoningEyeCount(data.getSummoningEyeCount() + 1);
        data.setTotalKills(data.getTotalKills() + data.getKills());
        data.setKills(-1); // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        saveValues();
    }

    /**
     * Resets all zealot counter statistics.
     */
    public void resetZealotCounter() {
        data.setSummoningEyeCount(0);
        data.setTotalKills(0);
        data.setKills(0);
        saveValues();
    }

    public void addOresMined() {
        data.setOresMined(data.getOresMined() + 1);
        SkyblockAddons.getInstance().getPlayerListener().setSavePersistentFlag(true);
    }

    public void addKills() {
        data.setKills(data.getKills() + 1);
        saveValues();
    }

    public void addSeaCreaturesKilled(int spawned) {
        data.setSeaCreaturesKilled(data.getSeaCreaturesKilled() + spawned);
        saveValues();
    }

    public void setLastTimeFetchur(long lastTimeFetchur) {
        data.setLastTimeFetchur(lastTimeFetchur);
        saveValues();
    }

    public Set<Integer> getLockedSlots() {
        String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
        return data.getProfileLockedSlots().computeIfAbsent(profile, k -> new HashSet<>());
    }

}