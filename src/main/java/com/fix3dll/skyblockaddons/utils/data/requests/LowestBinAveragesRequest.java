package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.utils.EnumUtils.LBinAveragesType;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LBIN_AVERAGES_TYPE;
import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LOWEST_BIN_PRICES_UPDATE_INTERVAL;

/**
 * @apiNote Averages update interval is double the lowest bin update interval
 */
public class LowestBinAveragesRequest extends RemoteFileRequest<Map<String, Double>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String BASE_URL = "https://moulberry.codes/auction_averages_lbin/";

    private static final AtomicBoolean apiAuctionAverageLBinError = new AtomicBoolean(false);
    private static final AtomicReference<ScheduledTask> updateTaskRef = new AtomicReference<>();

    public LowestBinAveragesRequest() {
        this((LBinAveragesType) Feature.ITEM_PRICES_IN_TOOLTIP.getAsEnum(LBIN_AVERAGES_TYPE));
    }

    private LowestBinAveragesRequest(LBinAveragesType type) {
        super(
                BASE_URL + type.getUrlPath(),
                new TypeToken<Map<String, Double>>() {}.getType(),
                new AuctionAverageLBinCallback(type),
                false,
                true
        );
    }

    /**
     * Starts or stops the scheduled polling cycle for the Lowest BIN Averages API.
     * <p>If {@code active} is {@code true}, an initial request is triggered to start the cycle,
     * but only if there is no currently scheduled update task. This safely prevents duplicate
     * network requests and overlapping background tasks.
     * <p>If {@code active} is {@code false}, any ongoing scheduled polling task is safely cancelled,
     * and the API error state is reset to ensure a clean slate for future activations.

     * @param active {@code true} to start the background data fetching cycle, {@code false} to stop and clean it up
     */
    public static void setActive(boolean active) {
        if (active) {
            if (updateTaskRef.get() == null) {
                DataUtils.loadOnlineData(new LowestBinAveragesRequest());
            }
            return;
        }

        ScheduledTask oldTask = updateTaskRef.getAndSet(null);
        if (oldTask != null) {
            oldTask.cancel();
            LOGGER.info("Auction average LBIN update task cancelled.");
        }

        apiAuctionAverageLBinError.set(false);
    }

    private static class AuctionAverageLBinCallback extends DataFetchCallback<Map<String, Double>> {

        private final LBinAveragesType type;

        public AuctionAverageLBinCallback(LBinAveragesType type) {
            super(LOGGER, URI.create(BASE_URL + type.getUrlPath()));
            this.type = type;
        }

        @Override
        public void completed(Map<String, Double> result) {
            super.completed(result);
            main.setLowestBinAveragesData(result);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("Auction average LBIN data loaded with '{}' entries", result.size());
            }

            if (apiAuctionAverageLBinError.compareAndSet(true, false)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiUpdated", "LBIN Averages"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        @Override
        public void failed(Throwable ex) {
            if (apiAuctionAverageLBinError.compareAndSet(false, true)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiError", "LBIN Averages"))
                                .withColor(ColorCode.RED.getColor())
                ));
            }
            LOGGER.catching(ex);
            scheduleNextUpdate();
        }

        private void scheduleNextUpdate() {
            int updateInterval = Math.max(
                    Feature.ITEM_PRICES_IN_TOOLTIP.getAsNumber(LOWEST_BIN_PRICES_UPDATE_INTERVAL).intValue(),
                    60
            );
            int delayTicks = (updateInterval + 1) * 20 * 2;

            ScheduledTask newTask = main.getScheduler().scheduleAsyncTask(scheduledTask -> {
                try {
                    DataUtils.loadOnlineData(new LowestBinAveragesRequest());
                } finally {
                    updateTaskRef.compareAndSet(scheduledTask, null);
                }
            }, delayTicks);

            ScheduledTask oldTask = updateTaskRef.getAndSet(newTask);
            if (oldTask != null) {
                oldTask.cancel();
            }

            LOGGER.debug("Next LBIN averages data ({}) update scheduled in {} ticks", type.getUrlPath(), delayTicks);
        }
    }

}