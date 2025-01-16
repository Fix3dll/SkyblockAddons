package codes.biscuit.skyblockaddons.features.discordrpc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.SkyblockDate;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordRPCManager implements IPCListener {

    @Getter @Setter private FeatureSetting currentStatus;

    private static final long APPLICATION_ID = 653443797182578707L;
    private static final long UPDATE_PERIOD = 4200L;

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private IPCClient client;
    private DiscordStatus detailsLine;
    private DiscordStatus stateLine;
    private long startTimestamp;

    private Timer updateTimer;
    private boolean connected;

    public void start() {
        SkyblockAddons.runAsync(() -> {
            try {
                LOGGER.info("Starting Discord RPC...");
                if (isActive()) {
                    return;
                }

                stateLine = (DiscordStatus) Feature.DISCORD_RPC.get(FeatureSetting.DISCORD_RP_STATE);
                detailsLine = (DiscordStatus) Feature.DISCORD_RPC.get(FeatureSetting.DISCORD_RP_DETAILS);
                startTimestamp = System.currentTimeMillis();
                client = new IPCClient(APPLICATION_ID);
                client.setListener(this);
                try {
                    client.connect();
                } catch (Exception ex) {
                    LOGGER.warn("Failed to connect to Discord RPC!");
                    LOGGER.catching(ex);
                }
            } catch (Throwable ex) {
                LOGGER.error("Discord RPC has thrown an unexpected error while trying to start...");
                LOGGER.catching(ex);
            }
        });
    }

    public void stop() {
        SkyblockAddons.runAsync(() -> {
            if (isActive()) {
                connected = false;
                client.close();
            }
        });
    }

    public boolean isActive() {
        return client != null && connected;
    }

    public void updatePresence() {
        String location = SkyblockAddons.getInstance().getUtils().getLocation();
        SkyblockDate skyblockDate = SkyblockAddons.getInstance().getUtils().getCurrentDate();
        String skyblockDateString = skyblockDate != null ? skyblockDate.toString() : "";

        // Early Winter 10th, 12:10am - Village
        String largeImageDescription = String.format("%s - %s", skyblockDateString, location);
        String smallImageDescription = String.format("Using SkyblockAddons v%s", SkyblockAddons.VERSION);
        RichPresence presence = new RichPresence.Builder()
                .setState(stateLine.getDisplayString(FeatureSetting.DISCORD_RP_CUSTOM_STATE))
                .setDetails(detailsLine.getDisplayString(FeatureSetting.DISCORD_RP_CUSTOM_DETAILS))
                .setStartTimestamp(startTimestamp)
                .setLargeImage(location.toLowerCase(Locale.ENGLISH).replaceAll(" ", "-"), largeImageDescription)
                .setSmallImage("skyblockicon", smallImageDescription)
                .setActivityType(ActivityType.Playing)
                .build();
        client.sendRichPresence(presence);
    }

    public void setStateLine(DiscordStatus status) {
        this.stateLine = status;
        if (isActive()) {
            updatePresence();
        }
    }

    public void setDetailsLine(DiscordStatus status) {
        this.detailsLine = status;
        if (isActive()) {
            updatePresence();
        }
    }

    private void cancelTimer() {
        if(updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    public void onReady(IPCClient client) {
        LOGGER.info("Discord RPC started.");
        connected = true;
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updatePresence();
            }
        }, 0, UPDATE_PERIOD);
    }

    @Override
    public void onClose(IPCClient client, JsonObject json) {
        LOGGER.info("Discord RPC closed.");
        this.client = null;
        connected = false;
        cancelTimer();
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        LOGGER.warn("Discord RPC disconnected.");
        this.client = null;
        connected = false;
        cancelTimer();
    }

    @Override
    public void onPacketSent(IPCClient client, Packet packet) {

    }

    @Override
    public void onPacketReceived(IPCClient client, Packet packet) {

    }

    @Override
    public void onActivityJoin(IPCClient client, String secret) {

    }

    @Override
    public void onActivitySpectate(IPCClient client, String secret) {

    }

    @Override
    public void onActivityJoinRequest(IPCClient client, String secret, User user) {

    }
}