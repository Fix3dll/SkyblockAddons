package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;

@Setter @Getter
public class PetCacheManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File petCacheFile;

    private PetCache petCache = new PetCache();

    public static class PetCache {
        @Setter private PetManager.Pet currentPet = null;

        /**
         * key = index + 45 * (pageNum - 1), value = {@link PetManager.Pet}
         * @see PetInfo
         */
        @Getter private final Int2ObjectOpenHashMap<PetManager.Pet> petMap = new Int2ObjectOpenHashMap<>();
    }

    public PetCacheManager(File mainConfigDir) {
        this.petCacheFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons/petCache.json");
    }

    /**
     * Loads the persistent values from {@code config/skyblockaddons/petCache.json} in the user's Minecraft folder.
     */
    public void loadValues() {
        if (petCacheFile.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(petCacheFile.toPath(), StandardCharsets.UTF_8)) {
                petCache = SkyblockAddons.getGson().fromJson(reader, PetCache.class);

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
     * Saves the pet cache to {@code config/skyblockaddons/petCache.json} in the user's Minecraft folder.
     */
    public void saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) LOGGER.info("Saving pet cache...");

            Path petCachePath = petCacheFile.toPath();
            Path tempPath = null;

            try {
                tempPath = Files.createTempFile(petCachePath.getParent(), petCachePath.getFileName().toString(), ".tmp");

                try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                    SkyblockAddons.getGson().toJson(petCache, writer);
                }

                Files.move(tempPath, petCachePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ex) {
                LOGGER.error("Error while saving pet cache!", ex);
                if (Minecraft.getInstance().player != null) {
                    Utils.sendErrorMessage(
                            "Error saving pet cache! Check log for more detail."
                    );
                }
            } finally {
                if (tempPath != null) {
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ex) {
                        LOGGER.warn("Failed to delete temp pet cache file: {}", tempPath, ex);
                    }
                }
                SAVE_LOCK.unlock();
            }

            if (isDevMode) LOGGER.info("Pet cache saved!");
        });
    }

    public PetManager.Pet getCurrentPet() {
        return petCache.currentPet;
    }

    public void setCurrentPet(PetManager.Pet pet) {
        setCurrentPet(pet, true);
    }

    public void setCurrentPet(PetManager.Pet pet, boolean updateEquipment) {
        if (pet == null || !pet.equals(petCache.currentPet)) {
            petCache.currentPet = pet;
            saveValues();
        }

        if (updateEquipment) {
            SkyblockEquipment eq = SkyblockEquipment.PET;

            if (pet == null && !eq.isEmpty()) {
                eq.setItemStack(eq.getEmptyStack());
                SkyblockEquipment.saveEquipments();
            } else if (pet != null && pet.getItemStack() != null
                    && !ItemStack.isSameItemSameComponents(pet.getItemStack(), eq.getItemStack())) {
                eq.setItemStack(pet.getItemStack());
                SkyblockEquipment.saveEquipments();
            }
        }
    }

    public PetManager.Pet getPet(int index) {
        return petCache.petMap.get(index);
    }

    public void putPet(int index, PetManager.Pet pet) {
        petCache.petMap.put(index, pet);
    }

    /**
     * Removes the pet at the given index from the cache.
     * @param index the slot index, computed as {@code index + 45 * (pageNum - 1)}
     * @return {@code true} if a pet was present and removed, {@code false} if the index was not in the cache
     */
    public boolean removePet(int index) {
        return petCache.petMap.remove(index) != null;
    }

}