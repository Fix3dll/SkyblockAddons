package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.events.PacketEvent;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.handlers.PacketHandler;
import codes.biscuit.skyblockaddons.core.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class NetworkListener {

    private static final Logger logger = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private ScheduledTask updateHealth;

    public NetworkListener() {
    }

    private final Cache<Integer, Integer> collectedCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

    @SubscribeEvent
    public void onSkyblockJoined(SkyblockJoinedEvent event) {
        logger.info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (Feature.DISCORD_RPC.isEnabled()) {
            main.getDiscordRPCManager().start();
        }
        updateHealth = main.getScheduler().scheduleTask(scheduledTask -> {
            main.getPlayerListener().updateLastSecondHealth();
        }, 0, 20);

        DataUtils.onSkyblockJoined();
    }

    @SubscribeEvent
    public void onSkyblockLeft(SkyblockLeftEvent event) {
        logger.info("Detected leaving skyblock!");
        main.getUtils().setOnSkyblock(false);
        main.getUtils().setProfileName("Unknown");
        if (main.getDiscordRPCManager().isActive()) {
            main.getDiscordRPCManager().stop();
        }
        if (updateHealth != null) {
            updateHealth.cancel();
            updateHealth = null;
        }
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        e.manager.channel().pipeline().addBefore("packet_handler", "sba_packet_handler", new PacketHandler());
        logger.info("Added SBA's packet handler to channel pipeline.");
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
    }

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.ReceiveEvent e) {
        if (!main.getUtils().isOnSkyblock()) return;
        Packet<?> packet = e.getPacket();

        // Java adoption of SkyHanni profit tracker
        if (packet instanceof S0DPacketCollectItem) {
            if (!SlayerTracker.getInstance().isTrackerEnabled()) return;

            EnumUtils.SlayerQuest activeQuest = main.getUtils().getSlayerQuest();
            if (activeQuest == null || !LocationUtils.isOnSlayerLocation(activeQuest)) return;

            int entityID = ((S0DPacketCollectItem) packet).getCollectedItemEntityID();
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityID);

            if (!(entity instanceof EntityItem)) return;
            EntityItem entityItem = (EntityItem) entity;

            if (collectedCache.getIfPresent(entityID) != null) return;
            collectedCache.put(entityID, 0);

            ItemStack itemStack = entityItem.getEntityItem();
            SlayerTracker.getInstance().addToTrackerData(
                    ItemUtils.getExtraAttributes(itemStack)
                    , itemStack.stackSize
                    , activeQuest
            );
        }
    }

    public static void setupModAPI() {
        HypixelModAPI modApi = HypixelModAPI.getInstance();
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                logger.info(packet.toString());
            }
            String mode = packet.getMode().orElse("null");
            main.getUtils().setMode(mode);
            main.getUtils().setMap(Island.getByMode(mode));
            main.getUtils().setServerID(packet.getServerName());
            if (packet.getServerType().orElse(null) == GameType.SKYBLOCK) {
                if (Feature.DISCORD_RPC.isEnabled() && !main.getDiscordRPCManager().isActive()) {
                    main.getDiscordRPCManager().start();
                }
            } else {
                if (main.getUtils().isOnSkyblock()) {
                    MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
                }
            }
        }).onError(reason -> {
            Utils utils = SkyblockAddons.getInstance().getUtils();
            utils.sendMessage("ModAPI packet failed: " + reason);
            utils.setMap(Island.UNKNOWN);
            utils.setMode("null");
        });
        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
    }

}