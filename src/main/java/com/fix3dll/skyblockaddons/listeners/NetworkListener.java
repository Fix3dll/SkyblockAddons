package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.events.PacketEvents;
import com.fix3dll.skyblockaddons.events.SkyblockEvents;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTracker;
import com.fix3dll.skyblockaddons.utils.EnumUtils.SlayerQuest;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class NetworkListener {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private ScheduledTask updateHealth;

    public NetworkListener() {
        SkyblockEvents.JOINED.register(this::onSkyblockJoined);
        SkyblockEvents.LEFT.register(this::onSkyblockLeft);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onPlayDisconnect);
        PacketEvents.READ.register(this::onPacketRead);
    }

    private final Cache<Integer, Integer> collectedCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

    private void onSkyblockJoined() {
        LOGGER.info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (Feature.DISCORD_RPC.isEnabled()) {
            main.getDiscordRPCManager().start();
        }
        updateHealth = main.getScheduler().scheduleTask(scheduledTask ->
            main.getPlayerListener().updateLastSecondHealth(), 0, 20
        );

        DataUtils.onSkyblockJoined();
    }

    private void onSkyblockLeft() {
        LOGGER.info("Detected leaving skyblock!");
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

    private void onPacketRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!main.getUtils().isOnSkyblock() || level == null) return;

        if (packet instanceof ClientboundTakeItemEntityPacket takeItemPacket) {
            if (!SlayerTracker.getInstance().isTrackerEnabled()) return;

            SlayerQuest activeQuest = main.getUtils().getSlayerQuest();
            if (activeQuest == null || !LocationUtils.isOnSlayerLocation(activeQuest)) return;

            int entityID = takeItemPacket.getItemId();
            Entity entity = level.getEntity(entityID);

            if (!(entity instanceof ItemEntity itemEntity)) return;

            if (collectedCache.getIfPresent(entityID) != null) return;
            collectedCache.put(entityID, 0);

            ItemStack itemStack = itemEntity.getItem();
            SlayerTracker.getInstance().addToTrackerData(
                    ItemUtils.getExtraAttributes(itemStack),
                    itemStack.getCount(),
                    activeQuest
            );
        }
    }

    private void onPlayDisconnect(ClientPacketListener clientPacketListener, Minecraft minecraft) {
        SkyblockEvents.LEFT.invoker().onSkyblockLeft();
    }

    public static void setupModAPI() {
        HypixelModAPI modApi = HypixelModAPI.getInstance();
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info(packet.toString());
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
                    SkyblockEvents.LEFT.invoker().onSkyblockLeft();
                }
            }
        }).onError(reason -> {
            Utils utils = SkyblockAddons.getInstance().getUtils();
            Utils.sendMessage("ModAPI packet failed: " + reason);
            utils.setMap(Island.UNKNOWN);
            utils.setMode("null");
        });
        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
    }

}
