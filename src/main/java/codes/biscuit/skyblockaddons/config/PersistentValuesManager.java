package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.FetchurManager;
import codes.biscuit.skyblockaddons.features.backpacks.CompressedStorage;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTrackerData;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTrackerData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Setter @Getter
public class PersistentValuesManager {

    private static final Logger logger = SkyblockAddons.getLogger();

    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File persistentValuesFile;

    private PersistentValues persistentValues = new PersistentValues();

    @Getter @Setter
    public static class PersistentValues {

        private int kills = 0; // Kills since last eye
        private int totalKills = 0; // Lifetime zealots killed
        private int summoningEyeCount = 0; // Lifetime summoning eyes

        private SlayerTrackerData slayerTracker = new SlayerTrackerData();
        private DragonTrackerData dragonTracker = new DragonTrackerData();
        private Map<String, CompressedStorage> storageCache = new HashMap<>();

        private int oresMined = 0;
        private int seaCreaturesKilled = 0;

        private long lastTimeFetchur = 0L; // Last time the player gave Fetchur the correct item in ms from epoch

//        private HypixelLanguage hypixelLanguage = HypixelLanguage.ENGLISH;
    }

    public PersistentValuesManager(File configDir) {
        this.persistentValuesFile = new File(configDir.getAbsolutePath() + "/skyblockaddons_persistent.cfg");
    }

    /**
     * Loads the persistent values from {@code config/skyblockaddons_persistent.cfg} in the user's Minecraft folder.
     */
    public void loadValues() {
        if (persistentValuesFile.exists()) {

            try (FileReader reader = new FileReader(persistentValuesFile)) {
                persistentValues = SkyblockAddons.getGson().fromJson(reader, PersistentValues.class);

                // If the file is completely empty because it is corrupted, Gson will return null
                if (persistentValues == null) {
                    persistentValues = new PersistentValues();
                }
            } catch (Exception ex) {
                logger.error("Error loading persistent values!", ex);
            }

        } else {
            saveValues();
        }
        FetchurManager.getInstance().postPersistentConfigLoad();
    }

    /**
     * Saves the persistent values to {@code config/skyblockaddons_persistent.cfg} in the user's Minecraft folder.
     */
    public void saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) logger.info("Saving persistent values...");

            try {
                //noinspection ResultOfMethodCallIgnored
                persistentValuesFile.createNewFile();

                try (FileWriter writer = new FileWriter(persistentValuesFile)) {
                    SkyblockAddons.getGson().toJson(persistentValues, writer);
                }
            } catch (Exception ex) {
                logger.error("Error saving persistent values!", ex);
                if (Minecraft.getMinecraft().thePlayer != null) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(
                            "Error saving persistent values! Check log for more detail."
                    );
                }
            }

            if (isDevMode) logger.info("Persistent values saved!");

            SAVE_LOCK.unlock();
        });
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
        saveValues();
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

}
