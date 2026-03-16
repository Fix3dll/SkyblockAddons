package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.BazaarData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BazaarRequest extends RemoteFileRequest<BazaarData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String PATH = "https://api.hypixel.net/v2/skyblock/bazaar";

    private static final AtomicBoolean apiBazaarError = new AtomicBoolean(false);
    private static final AtomicReference<ScheduledTask> updateTaskRef = new AtomicReference<>();

    public BazaarRequest() {
        super(
                PATH,
                new JSONResponseHandler<>(BazaarData.class),
                new BazaarCallback(),
                false,
                true
        );
    }

    /**
     * Starts or stops the scheduled Bazaar polling cycle.
     * <p>If {@code active} is {@code true}, an initial request is triggered
     * only when no scheduled update task exists.
     * <p>If {@code active} is {@code false}, any scheduled polling task is
     * canceled and the error state is reset.
     */
    public static void setActive(boolean active) {
        if (active) {
            if (updateTaskRef.get() == null) {
                DataUtils.loadOnlineData(new BazaarRequest());
            }
            return;
        }

        ScheduledTask oldTask = updateTaskRef.getAndSet(null);
        if (oldTask != null) {
            oldTask.cancel();
            LOGGER.info("Bazaar update task cancelled.");
        }

        apiBazaarError.set(false);
    }

    private static class BazaarCallback extends DataFetchCallback<BazaarData> {

        public BazaarCallback() {
            super(LOGGER, URI.create(PATH));
        }

        @Override
        public void completed(BazaarData result) {
            super.completed(result);
            main.setBazaarData(result);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("lastUpdated: {}, products: {}", new Date(result.getLastUpdated()), result.getProducts().size());
            }

            if (apiBazaarError.compareAndSet(true, false)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiUpdated", "Bazaar"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }
            scheduleNextUpdate(result.getLastUpdated());
        }

        @Override
        public void failed(Exception ex) {
            if (apiBazaarError.compareAndSet(false, true)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiError", "Bazaar"))
                                .withColor(ColorCode.RED.getColor())
                ));
            }
            scheduleNextUpdate(System.currentTimeMillis());
        }

        /**
         * Schedules the next fetch based on {@code lastUpdated} returned by the API,
         * using the interval configured in {@link FeatureSetting#BAZAAR_PRICES_UPDATE_INTERVAL} plus a 1s buffer.
         * Anchoring to the API's own timestamp keeps polling aligned with the server's update cycle.
         * <p>If the calculated time is in the past (e.g., due to stale API cache), a fallback delay
         * is applied to prevent rapid request spamming and rate-limit violations.
         */
        private void scheduleNextUpdate(long lastUpdated) {
            long updateInterval = Math.max(
                    Feature.ITEM_PRICES_IN_TOOLTIP.getAsNumber(FeatureSetting.BAZAAR_PRICES_UPDATE_INTERVAL).longValue(),
                    20
            );
            long nextUpdateTime = lastUpdated + (updateInterval * 1_000) + 1_000;
            long timeRemainingMs = nextUpdateTime - System.currentTimeMillis();

            int delayTicks;
            if (timeRemainingMs <= 0) {
                // API data is stale or time remaining is negative.
                // Fallback to 3 seconds (60 ticks) to prevent 0-tick request loops and HTTP 429 errors.
                delayTicks = 60;
                LOGGER.warn("Bazaar API data is stale or delay is negative. Applying fallback delay of 3 seconds.");
            } else {
                delayTicks = (int) (timeRemainingMs / 50);
            }

            ScheduledTask newTask = main.getScheduler().scheduleAsyncTask(scheduledTask -> {
                try {
                    DataUtils.loadOnlineData(new BazaarRequest());
                } finally {
                    updateTaskRef.compareAndSet(scheduledTask, null);
                }
            }, delayTicks);

            ScheduledTask oldTask = updateTaskRef.getAndSet(newTask);
            if (oldTask != null) {
                oldTask.cancel();
            }

            LOGGER.debug(
                    "Next bazaar update scheduled in {}ms (delay: {} ticks).",
                    timeRemainingMs <= 0 ? 3000 : timeRemainingMs, delayTicks
            );
        }
    }

}