package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.config.PetCacheManager;
import codes.biscuit.skyblockaddons.core.Rarity;
import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.mixins.hooks.FontRendererHook;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.MayorJerryData;
import codes.biscuit.skyblockaddons.utils.gson.RarityAdapter;
import codes.biscuit.skyblockaddons.utils.gson.UuidAdapter;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.ElectionData;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.OnlineData;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonManager;
import codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer;
import codes.biscuit.skyblockaddons.features.EntityOutlines.FeatureDungeonTeammateOutlines;
import codes.biscuit.skyblockaddons.features.EntityOutlines.FeatureItemOutlines;
import codes.biscuit.skyblockaddons.features.EntityOutlines.FeatureTrackerQuest;
import codes.biscuit.skyblockaddons.features.SkillXpManager;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordRPCManager;
import codes.biscuit.skyblockaddons.gui.screens.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.listeners.*;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.misc.Updater;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import codes.biscuit.skyblockaddons.utils.gson.GsonInitializableTypeAdapter;
import codes.biscuit.skyblockaddons.utils.gson.PatternAdapter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
@Mod(modid = "sbaunofficial",
        name = "SkyblockAddons Unofficial",
        version = "@VERSION@",
        clientSideOnly = true,
        acceptedMinecraftVersions = "@MOD_ACCEPTED@",
        guiFactory = "codes.biscuit.skyblockaddons.gui.SBAModGuiFactory")
public class SkyblockAddons {

    public static final String MOD_ID = "sbaunofficial";
    public static final String MOD_NAME = "SkyblockAddons";
    /**
     * If workingOnCi x.x.x.+x else x.x.x
     * @see "Gradle Build Script"
     */
    public static String VERSION = "@VERSION@";
    /**
     * This is set by the CI. If the build isn't done on CI, this will be an empty string.
     */
    public static final String BUILD_NUMBER = "@BUILD_NUMBER@";

    @Getter private static SkyblockAddons instance;
    @Getter private boolean fullyInitialized = false;
    @Getter private static final ZoneId hypixelZoneId = ZoneId.of("America/New_York");

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(EnumMap.class, (InstanceCreator<EnumMap>) type -> {
                Type[] types = (((ParameterizedType) type).getActualTypeArguments());
                return new EnumMap((Class<?>) types[0]);
            })
            .registerTypeAdapterFactory(new GsonInitializableTypeAdapter())
            .registerTypeAdapter(Pattern.class, new PatternAdapter())
            .registerTypeAdapter(Rarity.class, new RarityAdapter())
            .registerTypeAdapter(UUID.class, new UuidAdapter())
            .create();

    private static final Logger LOGGER = LogManager.getLogger(new SkyblockAddonsMessageFactory(MOD_NAME));

    private static final ThreadPoolExecutor THREAD_EXECUTOR = new ThreadPoolExecutor(
            0,
            1,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat(SkyblockAddons.MOD_NAME + " - #%d").build()
    );

    private ConfigValues configValues;
    private PersistentValuesManager persistentValuesManager;
    private PetCacheManager petCacheManager;
    private final PlayerListener playerListener;
    private final GuiScreenListener guiScreenListener;
    private final RenderListener renderListener;
    private final ResourceManagerReloadListener resourceManagerReloadListener;
    private final InventoryUtils inventoryUtils;
    private final Utils utils;
    private final Updater updater;
    private final DiscordRPCManager discordRPCManager;
    private final Scheduler scheduler;
    private final DungeonManager dungeonManager;
    private final SkillXpManager skillXpManager;

    @Setter private OnlineData onlineData;
    @Setter private ElectionData electionData;
    @Setter private MayorJerryData mayorJerryData;

    private boolean usingLabymod;
    private boolean usingOofModv1;
    private boolean usingPatcher;
    private final List<SkyblockKeyBinding> keyBindings = new LinkedList<>();

    @Getter
    private final Set<Integer> registeredFeatureIDs = new HashSet<>();

    public SkyblockAddons() {
        instance = this;

        playerListener = new PlayerListener();
        guiScreenListener = new GuiScreenListener();
        renderListener = new RenderListener();
        resourceManagerReloadListener = new ResourceManagerReloadListener();
        inventoryUtils = new InventoryUtils();
        utils = new Utils();
        updater = new Updater();
        scheduler = new Scheduler();
        dungeonManager = new DungeonManager();
        discordRPCManager = new DiscordRPCManager();
        skillXpManager = new SkillXpManager();
        electionData = new ElectionData();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        configValues = new ConfigValues(e.getModConfigurationDirectory());
        persistentValuesManager = new PersistentValuesManager(e.getModConfigurationDirectory());
        petCacheManager = new PetCacheManager(e.getModConfigurationDirectory());
        configValues.loadValues();
        DataUtils.readLocalAndFetchOnline();
        persistentValuesManager.loadValues();
        petCacheManager.loadValues();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new NetworkListener());
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(guiScreenListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);
        MinecraftForge.EVENT_BUS.register(new FeatureItemOutlines());
        MinecraftForge.EVENT_BUS.register(new FeatureDungeonTeammateOutlines());
        MinecraftForge.EVENT_BUS.register(new EntityOutlineRenderer());
        MinecraftForge.EVENT_BUS.register(new FeatureTrackerQuest());
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManagerReloadListener);

        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand());

        // Macs do not have a right control key.
        int developerModeKey = Minecraft.isRunningOnMac ? Keyboard.KEY_LMENU : Keyboard.KEY_RCONTROL;

        Collections.addAll(keyBindings,
                new SkyblockKeyBinding("open_settings", Keyboard.KEY_NONE, "settings.settings"),
                new SkyblockKeyBinding("edit_gui", Keyboard.KEY_NONE, "settings.editLocations"),
                new SkyblockKeyBinding("lock_slot", Keyboard.KEY_L, "settings.lockSlot"),
                new SkyblockKeyBinding("freeze_backpack", Keyboard.KEY_F, "settings.freezeBackpackPreview"),
                new SkyblockKeyBinding("increase_dungeon_map_zoom", Keyboard.KEY_ADD, "keyBindings.increaseDungeonMapZoom"),
                new SkyblockKeyBinding("decrease_dungeon_map_zoom", Keyboard.KEY_SUBTRACT, "keyBindings.decreaseDungeonMapZoom"),
                new SkyblockKeyBinding("copy_NBT", developerModeKey, "keyBindings.developerCopyNBT"));
        registerKeyBindings(keyBindings);
        setKeyBindingDescriptions();

        /*
         TODO: De-registering keys isn't standard practice. Should this be changed to have the player manually set it to
          KEY_NONE instead?

         De-register the devmode key binding since it's not needed until devmode is enabled. I can't just not register it
         in the first place since creating a KeyBinding object already adds it to the main key bind list. I need to manually
         de-register it so its default key doesn't conflict with other key bindings with the same key.
         */
        if (Feature.DEVELOPER_MODE.isDisabled()) {
            getDeveloperCopyNBTKey().deRegister();
        }

        usingLabymod = utils.isModLoaded("labymod");
        usingOofModv1 = utils.isModLoaded("refractionoof", "1.0");
        usingPatcher = utils.isModLoaded("patcher");

        NetworkListener.setupModAPI();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        for (Feature feature : Feature.values()) {
            if (feature.isColorFeature()) feature.getSettings().add(EnumUtils.FeatureSetting.COLOR);
        }

        if (Feature.FANCY_WARP_MENU.isEnabled()) {
            // Load in these textures so they don't lag the user loading them in later...
            for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(island.getResourceLocation());
            }
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(SkyblockAddonsGui.LOGO);
        Minecraft.getMinecraft().getTextureManager().bindTexture(SkyblockAddonsGui.LOGO_GLOW);
        fullyInitialized = true;
        FontRendererHook.onModInitialized();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            configValues.saveConfig();
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

    @Mod.EventHandler
    public void stop(FMLModDisabledEvent e) {
        discordRPCManager.stop();
    }

    public KeyBinding getOpenSettingsKey() {
        return keyBindings.get(0).getKeyBinding();
    }

    public KeyBinding getOpenEditLocationsKey() {
        return keyBindings.get(1).getKeyBinding();
    }

    public KeyBinding getLockSlotKey() {
        return keyBindings.get(2).getKeyBinding();
    }

    public KeyBinding getFreezeBackpackKey() {
        return keyBindings.get(3).getKeyBinding();
    }

    public SkyblockKeyBinding getDeveloperCopyNBTKey() {
        return keyBindings.get(6);
    }

    /**
     * Registers the given keybindings to the {@link net.minecraftforge.fml.client.registry.ClientRegistry}.
     *
     * @param keyBindings the keybindings to register
     */
    public void registerKeyBindings(List<SkyblockKeyBinding> keyBindings) {
        for (SkyblockKeyBinding keybinding: keyBindings) {
            keybinding.register();
        }
    }

    /**
     * This method updates keybinding descriptions to their localized name after registering them with a Minecraft-style
     * id, which is required for the set key to be saved properly in Minecraft settings.
     */
    public void setKeyBindingDescriptions() {
        for (SkyblockKeyBinding skyblockKeyBinding : keyBindings) {
            skyblockKeyBinding.getKeyBinding().keyDescription =
                    Translations.getMessage(skyblockKeyBinding.getTranslationKey());
        }
    }

    public static Gson getGson() {
        return GSON;
    }

    /**
     * Returns a {@link Logger} with the name of the calling class in the prefix, following the format
     * {@code [SkyblockAddons/className]}. Please call this method <b>once</b> in every class that needs a logger.
     * Do not call it multiple times in the same class to avoid creating un-needed {@code SkyblockAddonsMessageFactory}
     * instances.
     *
     * @return a {@code Logger} containing the name of the calling class in the prefix.
     */
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

    // This replaces the version placeholder if the mod is built using IntelliJ instead of Gradle.
    static {
        if (VERSION.contains("@")) { // Debug environment...
            VERSION = "1.6.0";
        }
    }
}
