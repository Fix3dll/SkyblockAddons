package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.commands.SkyblockAddonsCommand;
import com.fix3dll.skyblockaddons.config.ConfigValuesManager;
import com.fix3dll.skyblockaddons.config.ConfigValuesManager.ConfigValues;
import com.fix3dll.skyblockaddons.config.PersistentValuesManager;
import com.fix3dll.skyblockaddons.config.PetCacheManager;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.feature.FeatureData;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.features.SkillXpManager;
import com.fix3dll.skyblockaddons.features.discordrpc.DiscordRPCManager;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonManager;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.listeners.NetworkListener;
import com.fix3dll.skyblockaddons.listeners.PlayerListener;
import com.fix3dll.skyblockaddons.listeners.RenderListener;
import com.fix3dll.skyblockaddons.core.updater.Updater;
import com.fix3dll.skyblockaddons.core.scheduler.Scheduler;
import com.fix3dll.skyblockaddons.listeners.ScreenListener;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.InventoryUtils;
import com.fix3dll.skyblockaddons.utils.SkyblockAddonsMessageFactory;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ElectionData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.MayorJerryData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData;
import com.fix3dll.skyblockaddons.utils.gson.ConfigValuesAdapter;
import com.fix3dll.skyblockaddons.utils.gson.FeatureDataAdapter;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializableTypeAdapter;
import com.fix3dll.skyblockaddons.utils.gson.PatternAdapter;
import com.fix3dll.skyblockaddons.utils.gson.SemanticVersionAdapter;
import com.fix3dll.skyblockaddons.utils.gson.SkyblockRarityAdapter;
import com.fix3dll.skyblockaddons.utils.gson.UuidAdapter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
public class SkyblockAddons implements ClientModInitializer {
	public static final String MOD_ID = "skyblockaddons";
	public static final ModMetadata METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

	private static final Logger LOGGER = LogManager.getLogger(new SkyblockAddonsMessageFactory(METADATA.getName()));
	private static final ThreadPoolExecutor THREAD_EXECUTOR = new ThreadPoolExecutor(
			0,
			1,
			60L,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			new ThreadFactoryBuilder().setNameFormat(SkyblockAddons.METADATA.getName() + " - #%d").build()
	);
	@Getter
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(EnumMap.class, (InstanceCreator<EnumMap>) type -> {
				Type[] types = (((ParameterizedType) type).getActualTypeArguments());
				return new EnumMap((Class<?>) types[0]);
			})
			.registerTypeAdapterFactory(new GsonInitializableTypeAdapter())
			.registerTypeAdapter(Pattern.class, new PatternAdapter())
			.registerTypeAdapter(SemanticVersion.class, new SemanticVersionAdapter())
			.registerTypeAdapter(SkyblockRarity.class, new SkyblockRarityAdapter())
			.registerTypeAdapter(UUID.class, new UuidAdapter())
			.registerTypeAdapter(FeatureData.class, new FeatureDataAdapter())
			.registerTypeAdapter(ConfigValues.class, new ConfigValuesAdapter())
			.create();

	@Getter private static final ZoneId hypixelZoneId = ZoneId.of("America/New_York");
	@Getter private static SkyblockAddons instance;
	@Getter private boolean fullyInitialized = false;

	private final ConfigValuesManager configValuesManager;
	private final PersistentValuesManager persistentValuesManager;
	private final PetCacheManager petCacheManager;

	private final PlayerListener playerListener;
	private final DiscordRPCManager discordRPCManager;
	private final Utils utils;
	private final DungeonManager dungeonManager;
	private final Scheduler scheduler;
	private final Updater updater;
	private final RenderListener renderListener;
	private final InventoryUtils inventoryUtils;
	private final ScreenListener screenListener;
	private final NetworkListener networkListener;
	private final SkillXpManager skillXpManager;

	@Setter private OnlineData onlineData;
	@Setter private ElectionData electionData;
	@Setter private MayorJerryData mayorJerryData;

	private final HashSet<Integer> registeredFeatureIDs = new HashSet<>();

	public SkyblockAddons() {
		instance = this;
		DataUtils.readTexturedHeads();

		File configFile = FabricLoader.getInstance().getConfigDir().toFile();
		try {
			Files.createDirectories(Paths.get(configFile.getPath(), "/skyblockaddons"));
		} catch (IOException ex) {
			LOGGER.error("Could not create SkyblockAddons folder", ex);
		}
		configValuesManager = new ConfigValuesManager(configFile);
		persistentValuesManager = new PersistentValuesManager(configFile);
		petCacheManager = new PetCacheManager(configFile);
		configValuesManager.loadValues();
		updater = new Updater();
		persistentValuesManager.loadValues();
		petCacheManager.loadValues();

		playerListener = new PlayerListener();
		discordRPCManager = new DiscordRPCManager();
		utils = new Utils();
		dungeonManager = new DungeonManager();
		scheduler = new Scheduler();
		renderListener = new RenderListener();
		inventoryUtils = new InventoryUtils();
		screenListener = new ScreenListener();
		networkListener = new NetworkListener();
		skillXpManager = new SkillXpManager();

		electionData = new ElectionData();
		mayorJerryData = new MayorJerryData();

		DataUtils.readLocalAndFetchOnline();
	}

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");

		SkyblockAddonsCommand.initialize();

		ClientEvents.AFTER_INITIALIZATION.register(client -> {
			if (Feature.DEVELOPER_MODE.isDisabled()) {
				SkyblockKeyBinding.DEVELOPER_COPY_NBT.deRegister();
			}

			if (Feature.FANCY_WARP_MENU.isEnabled()) {
				// Load in these textures so they don't lag the user loading them in later...
				for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
					Minecraft.getInstance().getTextureManager().getTexture(island.getResourceLocation());
				}
			}

			DevUtils.resetEntityNamesToDefault(); // initialize class
			NetworkListener.setupModAPI();
			fullyInitialized = true;
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			configValuesManager.saveConfig();
			persistentValuesManager.saveValues();
			petCacheManager.saveValues();

			THREAD_EXECUTOR.shutdown();
			try {
				//noinspection ResultOfMethodCallIgnored
				THREAD_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				THREAD_EXECUTOR.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}, "SkyblockAddons-Shutdown"));
	}

	public static Logger getLogger() {
		String fullClassName = new Throwable().getStackTrace()[1].getClassName();
		String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

		return LogManager.getLogger(fullClassName, new SkyblockAddonsMessageFactory(simpleClassName));
	}

	/**
	 * Returns the time at which the function was called in the valid time zone of Hypixel Skyblock as immutable.
	 * @return ZonedDateTime
	 */
	public static ZonedDateTime getHypixelZonedDateTime() {
		return ZonedDateTime.now(hypixelZoneId);
	}

	public static void runAsync(Runnable runnable) {
		THREAD_EXECUTOR.execute(runnable);
	}
}