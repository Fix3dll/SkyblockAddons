package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.features.FetchurManager;
import codes.biscuit.skyblockaddons.features.backpacks.CompressedStorage;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTrackerData;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTrackerData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Setter @Getter
public class PersistentValuesManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File configFile;
    private final File persistentValuesFile;
    @Deprecated private final File legacyValuesFile; // TODO remove in future

    private PersistentValues persistentValues = new PersistentValues();

    @Getter @Setter
    public static class PersistentValues {

        private int kills = 0; // Kills since last eye
        private int totalKills = 0; // Lifetime zealots killed
        private int summoningEyeCount = 0; // Lifetime summoning eyes

        private SlayerTrackerData slayerTracker = new SlayerTrackerData();
        private DragonTrackerData dragonTracker = new DragonTrackerData();

        private final Map<String, CompressedStorage> storageCache = new HashMap<>();
        private final Map<String, Set<Integer>> profileLockedSlots = new HashMap<>();

        private int oresMined = 0;
        private int seaCreaturesKilled = 0;

        private long lastTimeFetchur = 0L; // Last time the player gave Fetchur the correct item in ms from epoch

//        private HypixelLanguage hypixelLanguage = HypixelLanguage.ENGLISH;
    }

    public PersistentValuesManager(File mainConfigDir) {
        this.configFile = mainConfigDir;
        this.persistentValuesFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons/persistentValues.json");
        this.legacyValuesFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons_persistent.cfg");
    }

    /**
     * Loads the persistent values from {@code config/skyblockaddons/persistentValues.json} in the user's Minecraft folder.
     */
    public void loadValues() {
        if (legacyValuesFile.exists()) {
            try {
                Files.move(legacyValuesFile.toPath(), persistentValuesFile.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to move legacy persistent values file", e);
            }
        }

        if (persistentValuesFile.exists()) {

            try (BufferedReader reader = Files.newBufferedReader(persistentValuesFile.toPath(), StandardCharsets.UTF_8)) {
                persistentValues = SkyblockAddons.getGson().fromJson(reader, PersistentValues.class);

                // If the file is completely empty because it is corrupted, Gson will return null
                if (persistentValues == null) {
                    persistentValues = new PersistentValues();
                }
            } catch (Exception ex) {
                LOGGER.error("Error loading persistent values!", ex);
                backupValues();
            }
        } else {
            saveValues();
        }
        FetchurManager.getInstance().postPersistentConfigLoad();
    }

    /**
     * Saves the persistent values to {@code config/skyblockaddons/persistentValues.json} in the user's Minecraft folder.
     */
    public void saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) LOGGER.info("Saving persistent values...");

            try {
                //noinspection ResultOfMethodCallIgnored
                persistentValuesFile.createNewFile();

                try (BufferedWriter writer = Files.newBufferedWriter(persistentValuesFile.toPath(), StandardCharsets.UTF_8)) {
                    SkyblockAddons.getGson().toJson(persistentValues, writer);
                }
            } catch (Exception ex) {
                LOGGER.error("Error saving persistent values!", ex);
                if (Minecraft.getMinecraft().thePlayer != null) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(
                            "Error saving persistent values! Check log for more detail."
                    );
                }
            }

            if (isDevMode) LOGGER.info("Persistent values saved!");
            SAVE_LOCK.unlock();
        });
    }

    /**
     * Creates backup of 'persistentValues.json'
     */
    public void backupValues() {
        if (!persistentValuesFile.exists()) {
            LOGGER.warn("persistentValues.json file for backup is not exist!");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            String formattedDate = ZonedDateTime.now().format(formatter);
            String backupFileName = "persistentValues.json." + formattedDate + ".backup";

            File backupFile = new File(configFile, "/skyblockaddons/backup/" + backupFileName);
            Files.createDirectories(backupFile.getParentFile().toPath());

            Files.copy(persistentValuesFile.toPath(), backupFile.toPath());
            LOGGER.info("Persistent values backed up successfully: {}", backupFile.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to backup persistent values file!", e);
        }
    }

    /**
     * Adds one to the summoning eye counter, adds the kills since last eye to the lifetime kill counter, and resets the kills since last eye counter.
     */
    public void addEyeResetKills() {
        persistentValues.summoningEyeCount++;
        persistentValues.totalKills += persistentValues.kills;
        persistentValues.kills = -1; // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        saveValues();
    }

    /**
     * Resets all zealot counter stats.
     */
    public void resetZealotCounter() {
        persistentValues.summoningEyeCount = 0;
        persistentValues.totalKills = 0;
        persistentValues.kills = 0;
        saveValues();
    }

    public void addOresMined() {
        persistentValues.oresMined++;
        SkyblockAddons.getInstance().getPlayerListener().setSavePersistentFlag(true);
    }

    public void addKills() {
        persistentValues.kills++;
        saveValues();
    }

    public void addSeaCreaturesKilled(int spawned) {
        persistentValues.seaCreaturesKilled += spawned;
        saveValues();
    }

    public void setLastTimeFetchur(long lastTimeFetchur) {
        persistentValues.lastTimeFetchur = lastTimeFetchur;
        saveValues();
    }

    public Set<Integer> getLockedSlots() {
        String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
        if (!persistentValues.profileLockedSlots.containsKey(profile)) {
            persistentValues.profileLockedSlots.put(profile, new HashSet<>());
        }

        return persistentValues.profileLockedSlots.get(profile);
    }

}
