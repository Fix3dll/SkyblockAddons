package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureData;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.enchants.EnchantManager;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigValuesManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private static final int CONFIG_VERSION = 11;

    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File configFile;
    private final File settingsConfigFile;

    /** Do not make direct changes! If you are using mutable objects, make a deep copy. */
    public static final EnumMap<Feature, FeatureData<?>> DEFAULT_FEATURE_DATA = new EnumMap<>(Feature.class);

    @Setter private boolean firstLoad = true;

    private ConfigValues configValues = new ConfigValues();

    @Setter
    public static class ConfigValues {

        private int configVersion = Integer.MIN_VALUE;
        private int lastFeatureId = Integer.MIN_VALUE;
        private EnumMap<Feature, FeatureData<?>> features = new EnumMap<>(Feature.class);

        public boolean isEmpty() {
            return configVersion == Integer.MIN_VALUE && lastFeatureId == Integer.MIN_VALUE && features.isEmpty();
        }
    }

    public ConfigValuesManager(File mainConfigDir) {
        this.configFile = mainConfigDir;
        this.settingsConfigFile = new File(mainConfigDir.getAbsolutePath(), "skyblockaddons/configurations.json");
    }

    public void loadValues() {
        Gson gson = SkyblockAddons.getGson();

        if (DEFAULT_FEATURE_DATA.isEmpty()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("defaults.json");
                 InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
                DEFAULT_FEATURE_DATA.putAll(
                        gson.fromJson(inputStreamReader, new TypeToken<EnumMap<Feature, FeatureData<?>>>() {}.getType())
                );
            } catch (Exception ex) {
                CrashReport crashReport = CrashReport.forThrowable(ex, "Reading default settings file");
                throw new ReportedException(crashReport);
            }
        }

        if (settingsConfigFile.exists()) {

            try (BufferedReader reader = Files.newBufferedReader(settingsConfigFile.toPath(), StandardCharsets.UTF_8)) {
                configValues = gson.fromJson(reader, ConfigValues.class);

                // If the file is completely empty because it is corrupted, Gson will return null
                if (configValues == null) {
                    configValues = new ConfigValues();
                    configValues.features.putAll(deepCopyDefaults());
                } else if (configValues.features.isEmpty()) {
                    configValues.features.putAll(deepCopyDefaults());
                }

                overwriteFeatureData();
            } catch (Exception ex) {
                if (configValues == null || configValues.isEmpty()) {
                    LOGGER.error("The configuration file is corrupt! After the previous file is backed up, it will be restored to default.", ex);
//                    DataUtils.getFailedRequests().put("configurations.json", ex); // TODO in-game error alerts

                    // Backup then restore defaults.
                    backupConfig(true);
                    addDefaultsAndSave();
                } else {
                    LOGGER.error("Error loading configuration values!", ex);
                }
            }
        } else {
            addDefaultsAndSave();
        }
        firstLoad = false;

        // Post load
        Feature.TURN_ALL_FEATURES_CHROMA.setEnabled(ColorUtils.areAllFeaturesChroma()); // also setEnabled saves config
        FontHook.setAllTextChroma(Feature.TURN_ALL_TEXTS_CHROMA.isChroma());
    }

    private void addDefaultsAndSave() {
        configValues.features.putAll(deepCopyDefaults());
        overwriteFeatureData();
        saveConfig();
    }

    private void overwriteFeatureData() {
        if (firstLoad) {
            // If the feature does not have any FeatureData, put the default FeatureData
            // It can be a new Feature or a manually edited Feature
            for (Feature feature : Feature.values()) {
                configValues.features.computeIfAbsent(feature, k -> DEFAULT_FEATURE_DATA.get(k).deepCopy());
            }
        }

        for (Map.Entry<Feature, FeatureData<?>> entry : configValues.features.entrySet()) {
            Feature feature = entry.getKey();
            FeatureData<?> featureData = entry.getValue();

            if (featureData != null) {
                if (firstLoad) firstLoadChecks(feature, featureData);
                feature.getFeatureData().overwriteData(featureData);
            } else {
                FeatureData<?> defaultData = DEFAULT_FEATURE_DATA.get(feature);
                if (defaultData != null) {
                    feature.getFeatureData().overwriteData(defaultData);
                    LOGGER.warn("Default FeatureData loaded for {} feature.", feature);
                } else {
                    throw new IllegalStateException("There is no default FeatureData for " + feature);
                }
            }
        }
    }

    private void firstLoadChecks(Feature feature, FeatureData<?> featureData) {
        TreeMap<FeatureSetting, Object> settings = featureData.getSettings();
        FeatureData<?> defaultFeatureData = DEFAULT_FEATURE_DATA.get(feature);
        TreeMap<FeatureSetting, Object> defaultSettings = defaultFeatureData.getSettings();

        if (featureData.getValue().getClass() != defaultFeatureData.getValue().getClass()) {
            featureData.setValue(defaultFeatureData.getValue());
            LOGGER.warn("The corrupted value of the '{}' Feature was reset to its default value.", feature);
        }

        if (settings == null && defaultSettings != null) {
            featureData.setSettings(new TreeMap<>());
            settings = featureData.getSettings();
        }

        if (settings != null && defaultSettings != null) {
            for (Map.Entry<FeatureSetting, Object> settingsEntry : defaultSettings.entrySet()) {
                FeatureSetting defaultSetting = settingsEntry.getKey();
                Object defaultValue = settingsEntry.getValue();

                if (!settings.containsKey(defaultSetting)) {
                    // New sub-setting entry
                    settings.put(defaultSetting, defaultValue);
                    LOGGER.info("Default '{}' FeatureSetting loaded for '{}' Feature.", defaultSetting, feature);
                } else if (defaultValue.getClass() != settings.get(defaultSetting).getClass()) {
                    // The sub-setting value is overridden by different type
                    settings.put(defaultSetting, defaultValue);
                    LOGGER.warn("'{}' FeatureSetting for '{}' Feature has been updated with new defaults!", defaultSetting, feature);
                }
            }
        }
    }

    public void saveConfig() {
        EnchantManager.markCacheDirty();
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) LOGGER.info("Saving config...");

            configValues.configVersion = CONFIG_VERSION;
            for (Feature feature : Feature.values()) {
                configValues.features.put(feature, feature.getFeatureData());
                if (feature.getId() > configValues.lastFeatureId) {
                    configValues.lastFeatureId = feature.getId();
                }
            }

            try {
                File tempFile = File.createTempFile(settingsConfigFile.getName(), ".tmp", settingsConfigFile.getParentFile());

                try (BufferedWriter writer = Files.newBufferedWriter(
                        tempFile.toPath(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                )) {
                    SkyblockAddons.getGson().toJson(configValues, writer);
                }

                Files.move(
                        tempFile.toPath(), settingsConfigFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
                );
            } catch (Exception ex) {
                LOGGER.error("Error saving configurations file!", ex);
                if (Minecraft.getInstance().player != null) {
                    Utils.sendErrorMessage(
                            "Error saving configurations file! Check log for more detail."
                    );
                }
            }

            SAVE_LOCK.unlock();
            if (isDevMode) LOGGER.info("Config saved!");
        });
    }

    /**
     * Sets the setting to the default value. Note that this method does not trigger {@link #saveConfig()} afterward.
     * @param setting Feature related setting
     * @exception NullPointerException if the default settings are not saved in defaults.json
     */
    public void setSettingToDefault(FeatureSetting setting) {
        Feature feature = setting.getRelatedFeature();
        if (feature != null) {
            FeatureData<?> data = Objects.requireNonNull(
                    DEFAULT_FEATURE_DATA.get(feature),
                    "There is no default FeatureData for " + feature
            );

            TreeMap<FeatureSetting, Object> settings = Objects.requireNonNull(
                    data.getSettings(),
                    "There is no default settings for " + feature
            );

            Object defaultValue = Objects.requireNonNull(
                    settings.get(setting),
                    "There is no default value for '" + setting + "' setting!"
            );

            if (feature.hasSettings()) {
                feature.set(setting, defaultValue);
            } else {
                feature.getFeatureData().setSettings(settings);
            }

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("{} has been set to default value.", setting.name());
            }

            return;
        }

        if (Feature.DEVELOPER_MODE.isEnabled()) {
            LOGGER.error("Could not reset {} to its default value.", setting.name());
        }
    }

    /**
     * Restore the value of the Feature to its default value. Note that this method does not trigger
     * {@link #saveConfig()} afterward.
     * @param feature the Feature whose value we want to restore
     */
    public void restoreFeatureDefaultValue(Feature feature) {
        FeatureData<?> defaultData = Objects.requireNonNull(
                DEFAULT_FEATURE_DATA.get(feature),
                "There is no default FeatureData for " + feature
        );
        Object defaultValue = Objects.requireNonNull(
                defaultData.getValue(),
                "There is no default value for '" + feature + "' Feature!"
        );
        feature.setValue(defaultValue);
        LOGGER.warn("The value of the '{}' Feature is reset to its default value.", feature);
    }

    public void setAllCoordinatesToDefault() {
        for (Map.Entry<Feature, FeatureData<?>> entry : DEFAULT_FEATURE_DATA.entrySet()) {
            Feature feature = entry.getKey();
            FeatureData<?> featureData = entry.getValue();

            if (featureData.getCoords() != null) {
                feature.getFeatureData().setCoords(featureData.getCoords().clonePair());
                feature.getFeatureData().setAnchorPoint(featureData.getAnchorPoint());
                feature.getFeatureData().setGuiScale(featureData.getGuiScale());
            }
        }
    }

    public void putDefaultCoordinates(Feature feature) {
        Pair<Float, Float> coords = DEFAULT_FEATURE_DATA.get(feature).getCoords();
        if (coords != null) {
            feature.getFeatureData().setCoords(coords.clonePair());
        }
    }

    public void putDefaultBarSizes() {
        for (Map.Entry<Feature, FeatureData<?>> entry : DEFAULT_FEATURE_DATA.entrySet()) {
            Feature feature = entry.getKey();
            FeatureData<?> featureData = entry.getValue();

            feature.getFeatureData().setBarSizes(featureData.getBarSizes().clonePair());
        }
    }

    public void putDefaultGuiScale(Feature feature) {
        float defaultScale = DEFAULT_FEATURE_DATA.get(feature).getGuiScale();
        feature.setGuiScale(defaultScale);
    }

    /**
     * @return deep copy of {@code DEFAULT_FEATURE_DATA}
     */
    public EnumMap<Feature, FeatureData<?>> deepCopyDefaults() {
        Gson gson = SkyblockAddons.getGson();
        String json = gson.toJson(DEFAULT_FEATURE_DATA);
        return gson.fromJson(json, new TypeToken<EnumMap<Feature, FeatureData<?>>>() {}.getType());
    }

    /**
     * Creates backup of 'configurations.json'
     */
    public void backupConfig(boolean corrupted) {
        if (!settingsConfigFile.exists()) {
            LOGGER.warn("configurations.json file for backup is not exist!");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            String formattedDate = ZonedDateTime.now().format(formatter);
            String extension = corrupted ? ".corrupted" : ".backup";
            String backupFileName = "configurations.json." + formattedDate + extension;

            File backupFile = new File(configFile, "/skyblockaddons/backup/" + backupFileName);
            Files.createDirectories(backupFile.getParentFile().toPath());

            Files.copy(settingsConfigFile.toPath(), backupFile.toPath());
            LOGGER.info("Configurations backed up successfully: {}", backupFile.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to backup configurations file!", e);
        }
    }

}