package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.SkyblockAddonsMessageFactory;
import com.fix3dll.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
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

public abstract class AbstractPersistentDataManager<T> {

    private final ReentrantLock saveLock = new ReentrantLock();

    protected final Logger logger;
    @Getter protected final File dataFile;
    private final Class<T> dataType;

    @Getter @Setter protected T data;

    /**
     * Initializes the persistent data manager.
     * @param mainConfigDir The root configuration directory.
     * @param fileName      The name of the JSON file, including extension (e.g., "petCache.json").
     * @param dataType      The class of the data model {@code T} used for Gson deserialization.
     */
    public AbstractPersistentDataManager(File mainConfigDir, String fileName, Class<T> dataType) {
        this.logger = LogManager.getLogger(
                getClass().getName(),
                new SkyblockAddonsMessageFactory(getClass().getSimpleName())
        );
        this.dataFile = new File(mainConfigDir.getAbsolutePath(), "/skyblockaddons/" + fileName);
        this.dataType = dataType;
        this.data = createDefault();
    }

    /**
     * Creates a default instance of the data model.
     * @return A new instance of type T.
     */
    protected abstract T createDefault();

    /**
     * Loads the persistent values from the JSON file.
     * If the file is missing or corrupted, a default instance is created.
     */
    public void loadValues() {
        if (dataFile.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(dataFile.toPath(), StandardCharsets.UTF_8)) {
                T loadedData = SkyblockAddons.getGson().fromJson(reader, dataType);
                this.data = loadedData != null ? loadedData : createDefault();
            } catch (Exception ex) {
                logger.error("Failed to load values from " + dataFile.getName() + "!", ex);
                handleLoadError();
                this.data = createDefault();
            }
        } else {
            saveValues();
        }
        onPostLoad();
    }

    /**
     * Saves the current data model to the JSON file asynchronously safely.
     */
    public void saveValues() {
        SkyblockAddons.runAsync(() -> {
            if (!saveLock.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) logger.info("Saving {}...", dataFile.getName());

            Path path = dataFile.toPath();
            Path tempPath = null;

            try {
                tempPath = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");

                try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                    SkyblockAddons.getGson().toJson(this.data, writer);
                }

                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ex) {
                logger.error("Error saving " + dataFile.getName() + "!", ex);
                if (Minecraft.getInstance().player != null) {
                    Utils.sendErrorMessage("Error saving " + dataFile.getName() + "! Check log for more detail.");
                }
            } finally {
                if (tempPath != null) {
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ex) {
                        logger.warn("Failed to delete temp file: {}", tempPath, ex);
                    }
                }
                saveLock.unlock();
            }

            if (isDevMode) logger.info("{} saved successfully!", dataFile.getName());
        });
    }

    /**
     * Hook method executed after the data is successfully loaded.
     * Can be overridden by subclasses to initialize transient states or notify other managers.
     */
    protected void onPostLoad() {
        // Default implementation does nothing
    }

    /**
     * Hook method executed when an error occurs during the loading process.
     * Useful for implementing backup logic in subclasses.
     */
    protected void handleLoadError() {
        // Default implementation does nothing
    }

}