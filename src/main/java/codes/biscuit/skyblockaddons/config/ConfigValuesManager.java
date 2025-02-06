package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.feature.FeatureData;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.features.enchants.EnchantLayout;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AnchorPoint;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigValuesManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private static final int CONFIG_VERSION = 11;

    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File configFile;
    private final File settingsConfigFile;
    @Deprecated private final File legacyConfigFile; // TODO remove in future

    /** Do not make direct changes! If you are using mutable objects, make a deep copy. */
    private final EnumMap<Feature, FeatureData<?>> DEFAULT_FEATURE_DATA = new EnumMap<>(Feature.class);

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
        this.legacyConfigFile = new File(mainConfigDir, "skyblockaddons.cfg");
    }

    // TODO migration map for Feature rework, remove in feature
    @Deprecated @Getter private static TreeMap<Feature, TreeMap<FeatureSetting, Integer>> migrationMap = null;

    public void loadValues() {
        Gson gson = SkyblockAddons.getGson();

        if (DEFAULT_FEATURE_DATA.isEmpty()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("defaults.json");
                 InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
                DEFAULT_FEATURE_DATA.putAll(
                        gson.fromJson(
                                inputStreamReader,
                                new TypeToken<EnumMap<Feature, FeatureData<?>>>() {}.getType()
                        )
                );
            } catch (Exception ex) {
                CrashReport crashReport = CrashReport.makeCrashReport(ex, "Reading default settings file");
                throw new ReportedException(crashReport);
            }
        }

        boolean configFileExists = settingsConfigFile.exists();
        // TODO It's terrifying down there. I don't recommend looking unless necessary. Destroy in the future.
        if (legacyConfigFile.exists() && !configFileExists) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("migrationMap.json");
                 InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
                migrationMap = gson.fromJson(
                        inputStreamReader,
                        new TypeToken<TreeMap<Feature, TreeMap<FeatureSetting, Integer>>>() {}.getType()
                );
            } catch (Exception ex) {
                CrashReport crashReport = CrashReport.makeCrashReport(ex, "Reading migration map file");
                throw new ReportedException(crashReport);
            }

            JsonObject legacyLoadedConfig;
            try (InputStreamReader reader = new InputStreamReader(
                    Files.newInputStream(legacyConfigFile.toPath()), StandardCharsets.UTF_8
            )) {
                JsonElement fileElement = new JsonParser().parse(reader);

                if (fileElement == null || fileElement.isJsonNull()) {
                    throw new JsonParseException("File is null!");
                }
                legacyLoadedConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                LOGGER.error("There was an error loading the config! Resetting all settings to default.");
                LOGGER.catching(ex);
                addDefaultsAndSave();
                return;
            }

            ArrayList<String> discordCustomStatuses = gson.fromJson(
                    legacyLoadedConfig.get("discordCustomStatuses"),
                    new TypeToken<ArrayList<String>>() {}.getType()
            );
            TreeSet<Integer> disabledFeaturesId = gson.fromJson(
                    legacyLoadedConfig.getAsJsonArray("disabledFeatures"),
                    new TypeToken<TreeSet<Integer>>() {}.getType()
            );
            HashSet<Integer> chromaFeatures = gson.fromJson(
                    legacyLoadedConfig.get("chromaFeatures"),
                    new TypeToken<HashSet<Integer>>() {}.getType()
            );
            HashMap<String, HashSet<Integer>> profileLockedSlots = gson.fromJson(
                    legacyLoadedConfig.get("profileLockedSlots"),
                    new TypeToken<HashMap<String, HashSet<Integer>>>() {}.getType()
            );
            HashMap<Integer, Integer> anchorPoints = gson.fromJson(
                    legacyLoadedConfig.get("anchorPoints"),
                    new TypeToken<HashMap<Integer, Integer>>() {}.getType()
            );
            HashMap<Integer, Integer> colors = gson.fromJson(
                    legacyLoadedConfig.get("colors"),
                    new TypeToken<HashMap<Integer, Integer>>() {}.getType()
            );
            TreeMap<Integer, Pair<Float, Float>> coordinates = new TreeMap<>();
            if (legacyLoadedConfig.has("coordinates")) {
                for (Map.Entry<String, JsonElement> element : legacyLoadedConfig.getAsJsonObject("coordinates").entrySet()) {
                    JsonArray coords = element.getValue().getAsJsonArray();
                    coordinates.put(
                            Integer.parseInt(element.getKey()),
                            new Pair<>(coords.get(0).getAsFloat(), coords.get(1).getAsFloat())
                    );
                }
            }
            HashMap<Integer, Pair<Float, Float>> barSizes = new HashMap<>();
            if (legacyLoadedConfig.has("barSizes")) {
                for (Map.Entry<String, JsonElement> element : legacyLoadedConfig.getAsJsonObject("barSizes").entrySet()) {
                    JsonArray coords = element.getValue().getAsJsonArray();
                    barSizes.put(
                            Integer.parseInt(element.getKey()),
                            new Pair<>(coords.get(0).getAsFloat(), coords.get(1).getAsFloat())
                    );
                }
            }
            HashMap<Integer, Float> guiScales = new HashMap<>();
            if (legacyLoadedConfig.has("guiScales")) {
                guiScales = gson.fromJson(
                        legacyLoadedConfig.get("guiScales"),
                        new TypeToken<HashMap<Integer, Float>>() {}.getType()
                );
            }

            EnumUtils.BackpackStyle backpackStyle = EnumUtils.BackpackStyle.GUI;
            if (legacyLoadedConfig.has("backpackStyle")) {
                backpackStyle = EnumUtils.BackpackStyle.values()[legacyLoadedConfig.get("backpackStyle").getAsInt()];
            }
            EnumUtils.DeployableDisplayStyle deployableDisplayStyle = EnumUtils.DeployableDisplayStyle.COMPACT;
            if (legacyLoadedConfig.has("petItemStyle")) {
                deployableDisplayStyle = EnumUtils.DeployableDisplayStyle.values()[legacyLoadedConfig.get("deployableStyle").getAsInt()];
            }
            EnumUtils.PetItemStyle petItemStyle = EnumUtils.PetItemStyle.SHOW_ITEM;
            if (legacyLoadedConfig.has("petItemStyle")) {
                petItemStyle = EnumUtils.PetItemStyle.values()[legacyLoadedConfig.get("petItemStyle").getAsInt()];
            }
            EnumUtils.TextStyle textStyle = EnumUtils.TextStyle.STYLE_ONE;
            if (legacyLoadedConfig.has("textStyle")) {
                textStyle = EnumUtils.TextStyle.values()[legacyLoadedConfig.get("textStyle").getAsInt()];
            }
            EnumUtils.ChromaMode chromaMode = EnumUtils.ChromaMode.FADE;
            if (legacyLoadedConfig.has("chromaMode")) {
                chromaMode = EnumUtils.ChromaMode.values()[legacyLoadedConfig.get("chromaMode").getAsInt()];
            }
            DiscordStatus discordStatus = DiscordStatus.LOCATION;
            if (legacyLoadedConfig.has("discordStatus")) {
                discordStatus = DiscordStatus.values()[legacyLoadedConfig.get("discordStatus").getAsInt()];
            }
            DiscordStatus discordDetails = DiscordStatus.AUTO_STATUS;
            if (legacyLoadedConfig.has("discordDetails")) {
                discordDetails = DiscordStatus.values()[legacyLoadedConfig.get("discordDetails").getAsInt()];
            }
            DiscordStatus discordAutoDefault = DiscordStatus.NONE;
            if (legacyLoadedConfig.has("discordAutoDefault")) {
                discordAutoDefault = DiscordStatus.values()[legacyLoadedConfig.get("discordAutoDefault").getAsInt()];
            }
            EnchantLayout enchantLayout = EnchantLayout.NORMAL;
            if (legacyLoadedConfig.has("enchantLayout")) {
                enchantLayout = EnchantLayout.values()[legacyLoadedConfig.get("enchantLayout").getAsInt()];
            }
            Language language = Language.ENGLISH;
            if (legacyLoadedConfig.has("language")) {
                String languageKey = legacyLoadedConfig.get("language").getAsString();
                language = Language.getFromPath(languageKey);
            }

            float mapZoom = 1.1F, chromaSaturation = 0.75F, chromaBrightness = 0.9F, chromaSpeed = 6F, healingCircleOpacity = 0.2F, chromaSize = 30F;
            int warningSeconds = 4, lastFeatureId;
            if (legacyLoadedConfig.has("mapZoom")) {
                mapZoom = legacyLoadedConfig.get("mapZoom").getAsFloat();
                if (mapZoom < 0.5F) { // legacy normalized zoom
                    mapZoom = 1.1F; // default value
                }
            }
            if (legacyLoadedConfig.has("chromaSaturation")) {
                chromaSaturation =legacyLoadedConfig.get("chromaSaturation").getAsFloat();
            }
            if (legacyLoadedConfig.has("chromaBrightness")) {
                chromaBrightness = legacyLoadedConfig.get("chromaBrightness").getAsFloat();
            }
            if (legacyLoadedConfig.has("chromaSpeed")) {
                chromaSpeed = legacyLoadedConfig.get("chromaSpeed").getAsFloat();
            }
            if (legacyLoadedConfig.has("healingCircleOpacity")) {
                healingCircleOpacity = legacyLoadedConfig.get("healingCircleOpacity").getAsFloat();
            }
            if (legacyLoadedConfig.has("chromaSize")) {
                chromaSize = legacyLoadedConfig.get("chromaSize").getAsFloat();
            }
            if (legacyLoadedConfig.has("warningSeconds")) {
                warningSeconds = legacyLoadedConfig.get("warningSeconds").getAsInt();
            }
            if (legacyLoadedConfig.has("lastFeatureId")) {
                lastFeatureId = legacyLoadedConfig.get("lastFeatureId").getAsInt();
            } else {
                lastFeatureId = Math.max(coordinates.lastKey(), disabledFeaturesId.last());
            }

            // migration
            for (Feature feature : Feature.values()) {
                // Exceptions
                switch (feature) {
                    case WARNING_TIME:
                        FeatureData<Integer> featureData = new FeatureData<>(feature.getFeatureGuiData());
                        featureData.setValue(warningSeconds);
                        feature.getFeatureData().overwriteData(featureData);
                        continue;
                    case TEXT_STYLE:
                        FeatureData<EnumUtils.TextStyle> featureData2 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData2.setValue(textStyle);
                        feature.getFeatureData().overwriteData(featureData2);
                        continue;
                    case CHROMA_MODE:
                        FeatureData<EnumUtils.ChromaMode> featureData3 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData3.setValue(chromaMode);
                        feature.getFeatureData().overwriteData(featureData3);
                        continue;
                    case CHROMA_SATURATION:
                        FeatureData<Float> featureData4 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData4.setValue(chromaSaturation);
                        feature.getFeatureData().overwriteData(featureData4);
                        continue;
                    case CHROMA_SIZE:
                        FeatureData<Float> featureData5 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData5.setValue(chromaSize);
                        feature.getFeatureData().overwriteData(featureData5);
                        continue;
                    case CHROMA_SPEED:
                        FeatureData<Float> featureData6 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData6.setValue(chromaSpeed);
                        feature.getFeatureData().overwriteData(featureData6);
                        continue;
                    case CHROMA_BRIGHTNESS:
                        FeatureData<Float> featureData7 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData7.setValue(chromaBrightness);
                        feature.getFeatureData().overwriteData(featureData7);
                        continue;
                    case LANGUAGE:
                        FeatureData<Language> featureData8 = new FeatureData<>(feature.getFeatureGuiData());
                        featureData8.setValue(language);
                        feature.getFeatureData().overwriteData(featureData8);
                        continue;
                }

                if (feature.getId() > lastFeatureId) {
                    LOGGER.warn("The '{}' Feature is set to its default settings.", feature);
                    feature.getFeatureData().overwriteData(DEFAULT_FEATURE_DATA.get(feature).deepCopy());
                    continue;
                }

                FeatureData<Boolean> newData = new FeatureData<>(feature.getFeatureGuiData());
                newData.setValue(!disabledFeaturesId.contains(feature.getId()));
                AnchorPoint anchor = AnchorPoint.fromId(anchorPoints.getOrDefault(feature.getId(), -1));
                if (anchor != null) {
                    newData.setAnchorPoint(anchor);
                }
                Pair<Float, Float> coords = coordinates.getOrDefault(feature.getId(), null);
                if (coords != null) {
                    newData.setCoords(coords);
                }
                Pair<Float, Float> barSize = barSizes.getOrDefault(feature.getId(), null);
                if (barSize != null) {
                    newData.setBarSizes(barSize);
                }
                float guiScale = guiScales.getOrDefault(feature.getId(), 1.0F);
                if (guiScale < 0.5F) { // legacy normalized scale
                    newData.setGuiScale(DEFAULT_FEATURE_DATA.get(feature).getGuiScale());
                } else if (guiScale != 1.0F) {
                    newData.setGuiScale(guiScale);
                }
                int legacyColor = colors.getOrDefault(feature.getId(), 0);
                if (legacyColor == 0) {
                    if (feature.isColorFeature()) {
                        legacyColor = feature.getFeatureGuiData().getDefaultColor().getColor(255);
                    } else {
                        legacyColor = ColorCode.RED.getColor(255);
                    }
                } else {
                    legacyColor = ColorUtils.setColorAlpha(legacyColor, 255);
                }
                newData.setColor(legacyColor);
                newData.setChroma(chromaFeatures.contains(feature.getId()));

                TreeMap<FeatureSetting, Object> settings = new TreeMap<>();
                migrationMap.forEach((mFeature, mSettingMap) -> {
                    if (mFeature != feature || mSettingMap == null || mSettingMap.isEmpty()) return;

                    for (Map.Entry<FeatureSetting, Integer> entry : mSettingMap.entrySet()) {
                        FeatureSetting legacySetting = entry.getKey();
                        int legacySettingFeatureId = entry.getValue();

                        if (legacySettingFeatureId > lastFeatureId) {
                            Object defaultValue = DEFAULT_FEATURE_DATA.get(mFeature).getSettings().get(legacySetting);
                            settings.put(legacySetting, defaultValue);
                            LOGGER.warn("The '{}' FeatureSetting is set to its default value.", legacySetting);
                        } else {
                            settings.put(legacySetting, !disabledFeaturesId.contains(legacySettingFeatureId));
                        }
                    }
                });
                // Exceptional settings
                switch (feature) {
                    case SHOW_BACKPACK_PREVIEW:
                        settings.put(FeatureSetting.BACKPACK_STYLE, backpackStyle);
                        break;
                    case PET_DISPLAY:
                        settings.put(FeatureSetting.PET_ITEM_STYLE, petItemStyle);
                        break;
                    case DEPLOYABLE_STATUS_DISPLAY:
                        settings.put(FeatureSetting.DEPLOYABLE_DISPLAY_STYLE, deployableDisplayStyle);
                        break;
                    case DUNGEONS_MAP_DISPLAY:
                        settings.put(FeatureSetting.DUNGEON_MAP_ZOOM, mapZoom);
                        break;
                    case SHOW_HEALING_CIRCLE_WALL:
                        settings.put(FeatureSetting.HEALING_CIRCLE_OPACITY, healingCircleOpacity);
                        break;
                    case ENCHANTMENT_LORE_PARSING:
                        settings.put(FeatureSetting.ENCHANT_LAYOUT, enchantLayout);
                        ColorCode comma = ColorCode.getByARGB(colors.getOrDefault(171, 2));
                        settings.put(FeatureSetting.COMMA_ENCHANT_COLOR, comma != null ? comma : ColorCode.BLUE);
                        ColorCode poor = ColorCode.getByARGB(colors.getOrDefault(168, 2));
                        settings.put(FeatureSetting.POOR_ENCHANT_COLOR, poor != null ? poor : ColorCode.GRAY);
                        ColorCode good = ColorCode.getByARGB(colors.getOrDefault(167, 2));
                        settings.put(FeatureSetting.GOOD_ENCHANT_COLOR, good != null ? good : ColorCode.BLUE);
                        ColorCode great = ColorCode.getByARGB(colors.getOrDefault(166, 2));
                        settings.put(FeatureSetting.GREAT_ENCHANT_COLOR, great != null ? great : ColorCode.GOLD);
                        ColorCode perfect = ColorCode.getByARGB(colors.getOrDefault(165, 2));
                        settings.put(FeatureSetting.PERFECT_ENCHANT_COLOR, perfect != null ? perfect : ColorCode.CHROMA);
                        break;
                    case DISCORD_RPC:
                        settings.put(FeatureSetting.DISCORD_RP_STATE, discordStatus);
                        settings.put(FeatureSetting.DISCORD_RP_DETAILS, discordDetails);
                        settings.put(FeatureSetting.DISCORD_RP_AUTO_MODE, discordAutoDefault);
                        if (discordCustomStatuses.size() > 1) {
                            settings.put(FeatureSetting.DISCORD_RP_CUSTOM_STATE, discordCustomStatuses.get(0));
                            settings.put(FeatureSetting.DISCORD_RP_CUSTOM_DETAILS, discordCustomStatuses.get(1));
                        }
                        break;
                }
                if (!settings.isEmpty()) {
                    newData.setSettings(settings);
                }
                firstLoadChecks(feature, newData);
                feature.getFeatureData().overwriteData(newData);
            }
            // Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR (173) FeatureSetting data to parent Feature
            Pair<Float, Float> proximityCoords = coordinates.getOrDefault(173, null);
            AnchorPoint proximityAnchor = AnchorPoint.fromId(anchorPoints.getOrDefault(173, -1));
            if (proximityCoords != null && proximityAnchor != null) {
                Feature.TREVOR_THE_TRAPPER_FEATURES.getFeatureData().setCoords(proximityCoords);
                Feature.TREVOR_THE_TRAPPER_FEATURES.getFeatureData().setAnchorPoint(proximityAnchor);
            }

            LOGGER.info("Backing up legacy config...");
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
                String formattedDate = ZonedDateTime.now().format(formatter);
                String backupFileName = "skyblockaddons-" + formattedDate + ".cfg";

                File backupFile = new File(legacyConfigFile.getParentFile(), "/skyblockaddons/backup/" + backupFileName);
                Files.createDirectories(backupFile.getParentFile().toPath());

                Files.move(legacyConfigFile.toPath(), backupFile.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to move legacy configurations file", e);
            }

            // move profileLockedSlots to PersistentValues
            main.getScheduler().scheduleTask(scheduledTask -> {
                if (main.isFullyInitialized()) {
                    PersistentValuesManager pvm = main.getPersistentValuesManager();
                    pvm.getPersistentValues().getProfileLockedSlots().putAll(profileLockedSlots);
                    pvm.saveValues();
                    main.getConfigValuesManager().saveConfig(); // Save configValues too
                    LOGGER.info("SkyblockAddons Unofficial new config migration completed.");
                    scheduledTask.cancel();
                }
            }, 20, 20);
        } else if (configFileExists) {

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
                    backupConfig();
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
    }

    private void addDefaultsAndSave() {
        configValues.features.putAll(deepCopyDefaults());
        overwriteFeatureData();
        saveConfig();
    }

    private void overwriteFeatureData() {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                //noinspection ResultOfMethodCallIgnored
                settingsConfigFile.createNewFile();

                try (BufferedWriter writer = Files.newBufferedWriter(settingsConfigFile.toPath(), StandardCharsets.UTF_8)) {
                    SkyblockAddons.getGson().toJson(configValues, writer);
                }
            } catch (Exception ex) {
                LOGGER.error("Error saving configurations file!", ex);
                if (Minecraft.getMinecraft().thePlayer != null) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(
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

    private void putDefaultCoordinates(Feature feature) {
        Pair<Float, Float> coords = DEFAULT_FEATURE_DATA.get(feature).getCoords().clonePair();
        if (coords != null) {
            feature.getFeatureData().setCoords(coords);
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

    public float getActualX(Feature feature) {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return getAnchorPoint(feature).getX(maxX) + getRelativeCoords(feature).getLeft();
    }

    public float getActualY(Feature feature) {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return getAnchorPoint(feature).getY(maxY) + getRelativeCoords(feature).getRight();
    }

    public Pair<Float, Float> getRelativeCoords(Feature feature) {
        Pair<Float, Float> coords = feature.getFeatureData().getCoords();
        if (coords != null) {
            return coords;
        } else {
            putDefaultCoordinates(feature);
            coords = feature.getFeatureData().getCoords();
            if (coords != null) {
                return coords;
            } else {
                return new Pair<>(0F, 0F);
            }
        }
    }

    public void setClosestAnchorPoint(Feature feature) {
        float x1 = getActualX(feature);
        float y1 = getActualY(feature);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int maxX = sr.getScaledWidth();
        int maxY = sr.getScaledHeight();
        double shortestDistance = -1;
        AnchorPoint closestAnchorPoint = AnchorPoint.BOTTOM_MIDDLE; // default
        for (AnchorPoint point : AnchorPoint.values()) {
            double distance = Point2D.distance(x1, y1, point.getX(maxX), point.getY(maxY));
            if (shortestDistance == -1 || distance < shortestDistance) {
                closestAnchorPoint = point;
                shortestDistance = distance;
            }
        }
        if (this.getAnchorPoint(feature) == closestAnchorPoint) {
            return;
        }
        float targetX = getActualX(feature);
        float targetY = getActualY(feature);
        float x = targetX-closestAnchorPoint.getX(maxX);
        float y = targetY-closestAnchorPoint.getY(maxY);
        feature.getFeatureData().setAnchorPoint(closestAnchorPoint);
        feature.getFeatureData().setCoords(x, y);
    }

    public AnchorPoint getAnchorPoint(Feature feature) {
        AnchorPoint anchorPoints = feature.getFeatureData().getAnchorPoint();
        if (anchorPoints != null) {
            return anchorPoints;
        } else {
            anchorPoints = DEFAULT_FEATURE_DATA.get(feature).getAnchorPoint();
            return anchorPoints == null ? AnchorPoint.BOTTOM_MIDDLE : anchorPoints;
        }
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
    public void backupConfig() {
        if (!settingsConfigFile.exists()) {
            LOGGER.warn("configurations.json file for backup is not exist!");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            String formattedDate = ZonedDateTime.now().format(formatter);
            String backupFileName = "configurations.json." + formattedDate + ".backup";

            File backupFile = new File(configFile, "/skyblockaddons/backup/" + backupFileName);
            Files.createDirectories(backupFile.getParentFile().toPath());

            Files.copy(settingsConfigFile.toPath(), backupFile.toPath());
            LOGGER.info("Configurations backed up successfully: {}", backupFile.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to backup configurations file!", e);
        }
    }
}