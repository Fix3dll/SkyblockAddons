package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.events.PacketEvent;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.handlers.PacketHandler;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class NetworkListener {

    private static final Logger logger = SkyblockAddons.getLogger();

    private final SkyblockAddons main;
    private ScheduledTask updateHealth;

    public NetworkListener() {
        main = SkyblockAddons.getInstance();
    }

    private final Cache<Integer, Integer> collectedCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        EVENT_BUS.post(new SkyblockLeftEvent());
    }

    @SubscribeEvent
    public void onSkyblockJoined(SkyblockJoinedEvent event) {
        logger.info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (main.getConfigValues().isEnabled(Feature.DISCORD_RPC)) {
            main.getDiscordRPCManager().start();
        }
        updateHealth = main.getNewScheduler().scheduleRepeatingTask(new SkyblockRunnable() {
            @Override
            public void run() {
                main.getPlayerListener().updateLastSecondHealth();
            }
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
            main.getNewScheduler().cancel(updateHealth);
            updateHealth = null;
        }
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        e.manager.channel().pipeline().addBefore("packet_handler", "sba_packet_handler", new PacketHandler());
        logger.info("Added SBA's packet handler to channel pipeline.");
    }

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.ReceiveEvent e) {
        if (!main.getUtils().isOnSkyblock()) return;
        Packet<?> packet = e.getPacket();

        // Java adoption of SkyHanni profit tracker
        if (packet instanceof S0DPacketCollectItem) {
            if (!SlayerTracker.getInstance().isTrackerEnabled()) return;
            if (main.getUtils().getSlayerQuest() == null) return;
            if (!LocationUtils.isSlayerLocation(main.getUtils().getSlayerQuest(), main.getUtils().getLocation())) return;

            int entityID = ((S0DPacketCollectItem) packet).getCollectedItemEntityID();
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityID);

            if (!(entity instanceof EntityItem)) return;
            EntityItem entityItem = (EntityItem) entity;

            if (collectedCache.getIfPresent(entityID) != null) return;
            collectedCache.put(entityID, 0);

            ItemStack itemStack = entityItem.getEntityItem();
            SlayerTracker.getInstance().addToTrackerData(ItemUtils.getExtraAttributes(itemStack), itemStack.stackSize);
        }
    }
}