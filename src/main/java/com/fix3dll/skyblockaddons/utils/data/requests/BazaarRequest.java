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
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.BazaarData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;

public class BazaarRequest extends RemoteFileRequest<BazaarData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String PATH = "https://api.hypixel.net/v2/skyblock/bazaar";

    private static boolean apiBazaarError = false;
    private static ScheduledTask updateTask;

    public BazaarRequest() {
        super(
                PATH,
                BazaarData.class,
                new BazaarCallback(),
                false,
                true
        );
    }

    /**
     * Cancels the scheduled update task.
     * To restart the update cycle, call {@code DataUtils.loadOnlineData(new BazaarRequest())}.
     */
    public static void cancelUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            apiBazaarError = false; // clear error cache too
            LOGGER.info("Bazaar update task cancelled.");
        }
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

            if (apiBazaarError) {
                apiBazaarError = false;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiBazaarUpdated"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }
            scheduleNextUpdate(result.getLastUpdated());
        }

        @Override
        public void failed(Throwable ex) {
            if (!apiBazaarError) {
                apiBazaarError = true;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiBazaarError"))
                                .withColor(ColorCode.RED.getColor())
                ));
            }
            scheduleNextUpdate(System.currentTimeMillis());
        }

        /**
         * Schedules the next fetch based on {@code lastUpdated} returned by the API,
         * using the interval configured in {@link FeatureSetting#BAZAAR_PRICES_UPDATE_INTERVAL} plus a 1s buffer.
         * Anchoring to the API's own timestamp keeps polling aligned with the server's update cycle.
         */
        private void scheduleNextUpdate(long lastUpdated) {
            if (updateTask != null) {
                updateTask.cancel();
            }

            // Bazaar endpoint updates approximately every 20 seconds; +1s buffer to avoid hitting stale data.
            long updateInterval = Feature.ITEM_PRICES_IN_TOOLTIP.getAsNumber(FeatureSetting.BAZAAR_PRICES_UPDATE_INTERVAL).longValue();
            long nextUpdateTime = lastUpdated + (updateInterval * 1_000) + 1_000;
            int delayTicks = (int) Math.max(0, (nextUpdateTime - System.currentTimeMillis()) / 50);

            updateTask = main.getScheduler().scheduleAsyncTask(
                    scheduledTask -> DataUtils.loadOnlineData(new BazaarRequest()),
                    delayTicks
            );

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info(
                        "Next bazaar update scheduled in {}ms (delay: {} ticks).",
                        nextUpdateTime - System.currentTimeMillis(), delayTicks
                );
            }
        }
    }

}