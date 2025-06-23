package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.PetInfo;
import codes.biscuit.skyblockaddons.features.PetManager;
import codes.biscuit.skyblockaddons.utils.Utils;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

@Setter @Getter
public class PetCacheManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    @Deprecated private final File legacyCacheFile; // TODO remove in future
    private final File petCacheFile;

    private PetCache petCache = new PetCache();

    public static class PetCache {
        private PetManager.Pet currentPet = null;

        /**
         * key = index + 45 * (pageNum - 1), value = {@link PetManager.Pet}
         * @see PetInfo
         */
        @Getter private final HashMap<Integer, PetManager.Pet> petMap = new HashMap<>();
    }

    public PetCacheManager(File mainConfigDir) {
        this.petCacheFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons/petCache.json");
        this.legacyCacheFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons_petCache.json");
    }

    /**
     * Loads the persistent values from {@code config/skyblockaddons/petCache.json} in the user's Minecraft folder.
     */
    public void loadValues() {
        if (legacyCacheFile.exists()) {
            try {
                Files.move(legacyCacheFile.toPath(), petCacheFile.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to move legacy pet cache file", e);
            }
        }

        if (petCacheFile.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(petCacheFile.toPath(), StandardCharsets.UTF_8)) {
                petCache = SkyblockAddons.getGson().fromJson(reader, PetCacheManager.PetCache.class);

                // If cache file is completely empty because it is corrupted, Gson will return null
                if (petCache == null) {
                    petCache = new PetCache();
                }
            } catch (Exception ex) {
                LOGGER.error("Error while loading pet cache!", ex);
            }
        } else {
            saveValues();
        }
    }

    /**
     * Saves the pet cache to {@code configconfig/skyblockaddons/petCache.json} in the user's Minecraft folder.
     */
    public void saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) LOGGER.info("Saving pet cache...");

            try {
                File tempFile = File.createTempFile(petCacheFile.getName(), ".tmp", petCacheFile.getParentFile());

                try (BufferedWriter writer = Files.newBufferedWriter(
                        tempFile.toPath(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                )) {
                    SkyblockAddons.getGson().toJson(petCache, writer);
                }

                Files.move(
                        tempFile.toPath(), petCacheFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
                );
            } catch (Exception ex) {
                LOGGER.error("Error while saving pet cache!", ex);
                if (Minecraft.getMinecraft().thePlayer != null) {
                    Utils.sendErrorMessage(
                            "Error saving pet cache! Check log for more detail."
                    );
                }
            }

            if (isDevMode) LOGGER.info("Pet cache saved!");
            SAVE_LOCK.unlock();
        });
    }

    public PetManager.Pet getCurrentPet() {
        return petCache.currentPet;
    }

    public void setCurrentPet(PetManager.Pet pet) {
        petCache.currentPet = pet;
        saveValues();
    }

    public PetManager.Pet getPet(int index) {
        return petCache.petMap.get(index);
    }

    public void putPet(int index, PetManager.Pet pet) {
        petCache.petMap.put(index, pet);
    }

}
