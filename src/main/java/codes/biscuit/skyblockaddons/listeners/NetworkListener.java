package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.events.PacketEvent;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.handlers.PacketHandler;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.Unpooled;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.HypixelPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.hypixel.modapi.packet.impl.serverbound.ServerboundRegisterPacket;
import net.hypixel.modapi.serializer.PacketSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NetworkListener {

    private static final Logger logger = SkyblockAddons.getLogger();

    private final SkyblockAddons main;
    private ScheduledTask updateHealth;

    // We store a local reference to the net handler, so it's instantly available from the moment we connect
    private static NetHandlerPlayClient netHandler;

    public NetworkListener() {
        main = SkyblockAddons.getInstance();
    }

    private final Cache<Integer, Integer> collectedCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

    @SubscribeEvent
    public void onSkyblockJoined(SkyblockJoinedEvent event) {
        logger.info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (Feature.DISCORD_RPC.isEnabled()) {
            main.getDiscordRPCManager().start();
        }
        updateHealth = main.getNewScheduler().scheduleRepeatingTask(new SkyblockRunnable() {
            @Override
            public void run() {
                main.getPlayerListener().updateLastSecondHealth();
            }
        }, 0, 20);

        DataUtils.onSkyblockJoined();
        HypixelModAPI.getInstance().sendPacket(
                new ServerboundRegisterPacket(
                        HypixelModAPI.getInstance().getRegistry(),
                        Collections.singleton("hyevent:location")
                )
        );
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
            main.getNewScheduler().cancel(updateHealth);
            updateHealth = null;
        }
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        e.manager.channel().pipeline().addBefore("packet_handler", "sba_packet_handler", new PacketHandler());
        logger.info("Added SBA's packet handler to channel pipeline.");
        netHandler = (NetHandlerPlayClient) e.handler;
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
        netHandler = null;
    }

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.ReceiveEvent e) {
        if (!main.getUtils().isOnSkyblock()) return;
        Packet<?> packet = e.getPacket();

        // Java adoption of SkyHanni profit tracker
        if (packet instanceof S0DPacketCollectItem) {
            if (!SlayerTracker.getInstance().isTrackerEnabled()) return;

            EnumUtils.SlayerQuest activeQuest = main.getUtils().getSlayerQuest();
            if (activeQuest == null || !LocationUtils.isSlayerLocation(activeQuest, main.getUtils().getLocation())) return;

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

        } else if (packet instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload payload = (S3FPacketCustomPayload) packet;
            String identifier = payload.getChannelName();

            if (HypixelModAPI.getInstance().getRegistry().isRegistered(identifier)) {
                PacketBuffer buffer = payload.getBufferData();
                buffer.retain();
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        HypixelModAPI.getInstance().handle(identifier, new PacketSerializer(buffer));
                    } catch (Exception ex) {
                        logger.warn("Failed to handle ModAPI packet {}", identifier, ex);
                    } finally {
                        buffer.release();
                    }
                });
            }
        }
    }

    public static boolean sendHypixelPacket(HypixelPacket packet) {
        if (netHandler == null) {
            return false;
        }

        if (!netHandler.getNetworkManager().isChannelOpen()) {
            logger.warn("Attempted to send packet while channel is closed!");
            netHandler = null;
            return false;
        }

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        PacketSerializer serializer = new PacketSerializer(buf);
        packet.write(serializer);
        netHandler.addToSendQueue(new C17PacketCustomPayload(packet.getIdentifier(), buf));
        return true;
    }

    public static void setupModAPI() {
        HypixelModAPI modApi = HypixelModAPI.getInstance();
        modApi.setPacketSender(NetworkListener::sendHypixelPacket);
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            SkyblockAddons main = SkyblockAddons.getInstance();
//            main.getUtils().sendMessage(packet.toString());
            main.getUtils().setServerID(packet.getServerName());
            String mode = packet.getMode().orElse("null");
            main.getUtils().setMap(Island.getByMode(mode));
            main.getUtils().setMode(mode);
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
            SkyblockAddons.getInstance().getUtils().sendMessage("Failed to send ModAPI packet: " + reason);
        });
        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
    }

}