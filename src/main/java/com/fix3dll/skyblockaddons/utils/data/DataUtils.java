package com.fix3dll.skyblockaddons.utils.data;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.Language;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.seacreatures.SeaCreature;
import com.fix3dll.skyblockaddons.core.seacreatures.SeaCreatureManager;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.exceptions.DataLoadingException;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.features.SkillXpManager;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.features.enchants.EnchantManager;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.requests.CompactorItemsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.ContainersRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.CooldownsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.EnchantmentsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.LocalizationsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.LocationsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.MayorRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.OnlineDataRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.PetItemsRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.SeaCreaturesRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.SkillXpRequest;
import com.fix3dll.skyblockaddons.utils.data.requests.SlayerLocationsRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.CompactorItem;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.EnchantmentsData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.LegacyIdItemMapData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.LocationData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.PetItem;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.TexturedHead;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionMetrics;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataUtils {

    private static final Gson GSON = SkyblockAddons.getGson();
    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(120 * 1000)
            .setConnectionRequestTimeout(120 * 1000)
            .setSocketTimeout(30 * 1000).build();

    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setUserAgent(Utils.USER_AGENT)
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
            .setRetryHandler(new RequestRetryHandler()).build();

    private static final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("SBA DataUtils Thread %d")
                    .setUncaughtExceptionHandler(new UncaughtFetchExceptionHandler()).build();

    private static final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);

    private static final FutureRequestExecutionService futureRequestExecutionService =
            new FutureRequestExecutionService(httpClient, executorService);

    @Getter
    private static final FutureRequestExecutionMetrics executionServiceMetrics =
            futureRequestExecutionService.metrics();

    private static final ArrayList<RemoteFileRequest<?>> remoteRequests = new ArrayList<>();

    private static final TreeMap<String, Throwable> failedRequests = new TreeMap<>();

    static boolean fallbackCDNUsed = false;

    static final HashSet<String> failedUris = new HashSet<>();

    // Whether the failed requests error was shown in chat,
    // used to make it show only once per session except reloadRes command
    private static boolean failureMessageShown = false;

    /**
     * The mod uses the online data files if this is {@code true} and local data if this is {@code false}.
     * This is set to {@code true} if the mod is running in production or if it's running in a dev environment that has
     * the environment variable {@code FETCH_DATA_ONLINE}.
     */
    public static final boolean USE_ONLINE_DATA = !FabricLoader.getInstance().isDevelopmentEnvironment() ||
            Boolean.getBoolean("sba.data.online");

    private static String path;

    private static LocalizationsRequest localizedStringsRequest = null;

    static {
        connectionManager.setMaxTotal(5);
        connectionManager.setDefaultMaxPerRoute(5);
        registerNewRemoteRequests();
    }

    //TODO: Migrate all data file loading to this class
    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used, unless the environment variable "FETCH_DATA_ONLINE" is present.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();
        DataUtils.loadOnlineData(new MayorRequest()); // API data

        if (USE_ONLINE_DATA) {
            fetchFromOnline();
        } else {
            SkyblockAddons.getInstance().getUpdater().checkForUpdate();
        }
    }

    /**
     * Reads textured heads. Separated from {@link #readLocalFileData()} for early loading.
     */
    public static void readTexturedHeads() {
        try (InputStream inputStream = DataUtils.class.getResourceAsStream("/texturedHeads.json");
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            ItemUtils.setTexturedHeads(GSON.fromJson(inputStreamReader, new TypeToken<Object2ObjectOpenHashMap<String, TexturedHead>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }
    }

    /**
     * Reads local json files before pulling from online
     */
    public static void readLocalFileData() {
        // Online Data
        path = "/data.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            main.setOnlineData(GSON.fromJson(inputStreamReader, OnlineData.class));
        } catch (Exception ex) {
            handleLocalFileReadException(path, ex);
        }

        // Localized Strings
        loadLocalizedStrings(false);

        // Default language en_US
        try (InputStream inputStream = DataUtils.class.getClassLoader().getResourceAsStream("lang/en_US.json");
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            Translations.setDefaultLangJson(GSON.fromJson(inputStreamReader, JsonObject.class));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Containers
        path = "/containers.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            ItemUtils.setContainers(GSON.fromJson(inputStreamReader, new TypeToken<Object2ObjectOpenHashMap<String, ContainerData>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Compactor Items
        path = "/compactorItems.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            Object2ObjectOpenHashMap<String, CompactorItem> compactorItems = GSON.fromJson(
                    inputStreamReader,
                    new TypeToken<Object2ObjectOpenHashMap<String, CompactorItem>>() {}.getType()
            );
            compactorItems.forEach((skyblockId, compactorItem) ->
                    ItemUtils.setItemStackSkyblockID(compactorItem.getItemStack(), skyblockId)
            );
            ItemUtils.setCompactorItems(compactorItems);
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Sea Creatures
        path = "/seaCreatures.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            SeaCreatureManager.getInstance().setSeaCreatures(GSON.fromJson(inputStreamReader, new TypeToken<Map<String, SeaCreature>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Enchantment data
        path = "/enchants.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            EnchantManager.setEnchants(GSON.fromJson(inputStreamReader, new TypeToken<EnchantmentsData>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Cooldown Data
        path = "/cooldowns.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            Object2IntOpenHashMap<String> cooldowns = new Object2IntOpenHashMap<>();
            cooldowns.putAll(GSON.fromJson(inputStreamReader, new TypeToken<HashMap<String, Integer>>() {}.getType()));
            CooldownManager.setItemCooldowns(cooldowns);
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Skill xp Data
        path = "/skillXp.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            SkillXpManager.initialize(GSON.fromJson(inputStreamReader, SkillXpManager.JsonInput.class));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Pet Items Data
        path = "/petItems.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            PetManager.setPetItems(GSON.fromJson(inputStreamReader, new TypeToken<HashMap<String, PetItem>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Locations Data
        path = "/locations.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)){
            HashMap<String, LocationData> result = GSON.fromJson(
                    inputStreamReader, new TypeToken<HashMap<String, LocationData>>() {}.getType()
            );
            for (Map.Entry<String, LocationData> entry : result.entrySet()) {
                for (Island island : Island.values()) {
                    if (island.getMode().equalsIgnoreCase(entry.getKey())) {
                        island.setLocationData(entry.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Slayer Locations Data
        path = "/slayerLocations.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)){
            LocationUtils.setSlayerLocations(
                    GSON.fromJson(inputStreamReader, new TypeToken<HashMap<String, Set<String>>>() {}.getType())
            );
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        if (LegacyIdItemMapData.getItemStack("175:4").getItem() == Items.ROSE_BUSH) {
            LOGGER.info("roses are red and violets are blue");
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        for (RemoteFileRequest<?> request : remoteRequests) {
            request.execute(futureRequestExecutionService);
            if (request.getURL().contains(DataConstants.CDN_BASE_URL)) {
                SkyblockAddons.getInstance().getScheduler().scheduleAsyncTask(scheduledTask -> {
                    if (request.isDone() && failedUris.contains(request.getURL())) {
                        request.setFallbackCDN();
                        request.execute(futureRequestExecutionService);

                        if (!fallbackCDNUsed) {
                            if (Minecraft.getInstance().player != null) {
                                Utils.sendMessage(Translations.getMessage("messages.fallbackCdnUsed"));
                            } else {
                                LOGGER.warn(Translations.getMessage("messages.fallbackCdnUsed"));
                            }
                            fallbackCDNUsed = true;
                        }

                        scheduledTask.cancel();
                    }
                }, 0, 2);
            }
        }
    }

    public static void loadOnlineData(RemoteFileRequest<?> request) {
        request.execute(futureRequestExecutionService);
    }

    /**
     * Loads the localized strings for the current {@link Language} set in the mod settings with the choice of loading
     * only local strings or local and online strings.
     *
     * @param loadOnlineStrings Loads local and online strings if {@code true}, loads only local strings if {@code false}
     */
    public static void loadLocalizedStrings(boolean loadOnlineStrings) {
        Language currentLanguage = (Language) Feature.LANGUAGE.getValue();
        loadLocalizedStrings(currentLanguage, loadOnlineStrings);
    }

    /**
     * Loads the localized strings for the given {@link Language} with the choice of loading only local strings or local
     * and online strings. Languages are handled separately from other files because they may need to be loaded multiple
     * times in-game instead of just on startup. Online strings will never be loaded for English, regardless of the value
     * of {@code loadOnlineStrings}.
     *
     * @param language the {@code Language} to load strings for
     * @param loadOnlineStrings Loads local and online strings if {@code true}, loads only local strings if {@code false},
     *                          does not override {@link DataUtils#USE_ONLINE_DATA}
     */
    public static void loadLocalizedStrings(Language language, boolean loadOnlineStrings) {
        // logger.info("Loading localized strings for " + language.name() + "...");

        path = "lang/" + language.getPath() + ".json";
        try (InputStream inputStream = DataUtils.class.getClassLoader().getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            Translations.setLanguageJson(GSON.fromJson(inputStreamReader, JsonObject.class));
            Feature.LANGUAGE.setValue(language);
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        if (USE_ONLINE_DATA && loadOnlineStrings && language != Language.ENGLISH) {
            if (localizedStringsRequest != null) {
                HttpRequestFutureTask<JsonObject> futureTask = localizedStringsRequest.getFutureTask();
                if (!futureTask.isDone()) {
                    futureTask.cancel(false);
                }
            }

            localizedStringsRequest = new LocalizationsRequest(language);
            localizedStringsRequest.execute(futureRequestExecutionService);
        }

        // logger.info("Finished loading localized strings.");
    }

    /**
     * Displays a message when the player first joins Skyblock.
     */
    public static void onSkyblockJoined() {
        if (!failureMessageShown && !failedRequests.isEmpty()) {
            StringBuilder errorMessageBuilder = new StringBuilder("Failed Requests:\n");

            for (Map.Entry<String, Throwable> failedRequest : failedRequests.entrySet()) {
                errorMessageBuilder.append(failedRequest.getKey()).append("\n");
                errorMessageBuilder.append(failedRequest.getValue().toString()).append("\n");
            }

            MutableComponent failureMessageComponent = Component.literal(
                    Translations.getMessage(
                            "messages.fileFetchFailed",
                            ColorCode.AQUA + SkyblockAddons.METADATA.getName() + ColorCode.RED,
                            failedRequests.size()
                    )
            );

            MutableComponent buttonRowComponent = Component.literal("[" + Translations.getMessage("messages.copy") + "]").withStyle(style ->
                    style.withClickEvent(
                            new ClickEvent.RunCommand(
                                    ColorCode.WHITE + String.format("/sba internal copy %s", errorMessageBuilder)
                            )
                    )
            );
            buttonRowComponent.append("  ");
            buttonRowComponent.append(Component.literal("[" + Translations.getMessage("messages.retry") + "]")
                    .withStyle(style ->
                            style.withClickEvent(
                                    new ClickEvent.RunCommand(ColorCode.WHITE + "/sba reloadRes")
                            )
                    )
            );
            failureMessageComponent.append("\n").append(buttonRowComponent);

            Utils.sendMessage(failureMessageComponent, false);
            failureMessageShown = true;
            failedRequests.clear();
        }
    }

    /**
     * Returns the file name from the end of a given URL string.
     * This does not check if the URL has a valid file name at the end.
     *
     * @param url the URL string to get the file name from
     * @return the file name from the end of the URL string
     */
    static String getFileNameFromUrlString(String url) {
        int fileNameIndex = url.lastIndexOf('/') + 1;
        int queryParamIndex = url.indexOf('?', fileNameIndex);
        return url.substring(fileNameIndex, queryParamIndex > fileNameIndex ? queryParamIndex : url.length());
    }

    /**
     * After clearing the list, it constructs new requests and adds them to the list.
     * It also resets the {@link DataUtils#fallbackCDNUsed}, {@link DataUtils#failureMessageShown} and
     * {@link DataUtils#failedUris} values.
     */
    public static void registerNewRemoteRequests() {
        remoteRequests.clear();
        failedUris.clear();
        fallbackCDNUsed = false;
        failureMessageShown = false;

        remoteRequests.add(new OnlineDataRequest());
//        if (SkyblockAddons.getInstance().getConfigValues().getLanguage() != Language.ENGLISH) {
//            remoteRequests.add(new LocalizedStringsRequest(SkyblockAddons.getInstance().getConfigValues().getLanguage()));
//        }
        remoteRequests.add(new ContainersRequest());
        remoteRequests.add(new CompactorItemsRequest());
        remoteRequests.add(new SeaCreaturesRequest());
        remoteRequests.add(new EnchantmentsRequest());
        remoteRequests.add(new CooldownsRequest());
        remoteRequests.add(new SkillXpRequest());
        remoteRequests.add(new PetItemsRequest());
        remoteRequests.add(new LocationsRequest());
        remoteRequests.add(new SlayerLocationsRequest());
    }

    /**
     * This method handles errors that can occur when reading the local configuration files.
     * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
     * {@code Throwable} in the console.
     * If the game is initialized, it crashes the game with a crash report containing the file path and the stacktrace
     * of the given {@code Throwable}.
     *
     * @param filePath the path to the file that caused the exception
     * @param exception the exception that occurred
     */
    public static void handleLocalFileReadException(String filePath, Throwable exception) {
        if (ClientEvents.ClientInitialization.AFTER_INITIALIZE.isTrue()) {
            throw new DataLoadingException(filePath, exception);
        } else {
            CrashReport crashReport = CrashReport.forThrowable(
                    exception, "[SkyblockAddons] Loading data file at %s".formatted(filePath)
            );
            throw new ReportedException(crashReport);
        }
    }

    /**
     * This method handles errors that can occur when reading the online configuration files.
     * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
     * {@code Throwable} in the console.
     * If the game is initialized, it crashes the game with a crash report containing the file name and the stacktrace
     * of the given {@code Throwable}.
     *
     * @param urlString the requestPath for the file that failed to load
     * @param exception the exception that occurred
     */
    static void handleOnlineFileLoadException(String urlString, Throwable exception, boolean essential) {
        String fileName = getFileNameFromUrlString(urlString);
        failedRequests.put(urlString, exception);

        // The loader encountered a file name it didn't expect.
        if (exception instanceof IllegalArgumentException) {
            LOGGER.error(exception.getMessage());
            return;
        }

        if (essential) {
            if (ClientEvents.ClientInitialization.AFTER_INITIALIZE.isTrue()) {
                throw new DataLoadingException(urlString, exception);
            } else {
                // Don't include URL because Fire strips URLs.
                CrashReport crashReport = CrashReport.forThrowable(
                        exception,
                        String.format("Loading online data file at %s", fileName)
                );
                throw new ReportedException(crashReport);
            }
        } else {
            LOGGER.error("Failed to load \"{}\" from the server. The local copy will be used instead.", fileName);
            if (exception != null) {
                LOGGER.error(exception.getMessage());
            }
        }
    }
}
