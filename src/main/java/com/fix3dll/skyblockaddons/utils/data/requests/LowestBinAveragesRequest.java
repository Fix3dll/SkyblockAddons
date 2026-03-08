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
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LBIN_AVERAGES_TYPE;
import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LOWEST_BIN_PRICES_UPDATE_INTERVAL;

/**
 * @apiNote Averages update interval is double the lowest bin update interval
 */
public class LowestBinAveragesRequest extends RemoteFileRequest<Map<String, Double>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String BASE_URL = "https://moulberry.codes/auction_averages_lbin/";

    private static volatile boolean apiAuctionAverageLBinError = false;
    private static volatile ScheduledTask updateTask;

    public LowestBinAveragesRequest() {
        super(
                buildUrl(),
                new JSONResponseHandler<>(new TypeToken<Map<String, Double>>() {}.getType(), true),
                new AuctionAverageLBinCallback(),
                false,
                true
        );
    }

    /**
     * Builds the full endpoint URL from the currently selected {@link LBinAveragesType} setting.
     */
    private static String buildUrl() {
        LBinAveragesType type = (LBinAveragesType) Feature.ITEM_PRICES_IN_TOOLTIP.getAsEnum(LBIN_AVERAGES_TYPE);
        return BASE_URL + type.getUrlPath();
    }

    /**
     * Starts or stops the scheduled fetch cycle for this request.
     *
     * <p>If {@code active} is {@code true} and no update task is currently running,
     * a new fetch is initiated immediately. If {@code active} is {@code false} and
     * a task is running, it is canceled and the error state is cleared.
     * @param active {@code true} to start the fetch cycle, {@code false} to stop it
     */
    public static void setActive(boolean active) {
        if (active) {
            if (updateTask == null) {
                DataUtils.loadOnlineData(new LowestBinAveragesRequest());
            }
        } else {
            if (updateTask != null) {
                updateTask.cancel();
                updateTask = null;
                apiAuctionAverageLBinError = false; // clear error cache too
                LOGGER.info("Auction average LBIN update task cancelled.");
            }
        }
    }

    private static class AuctionAverageLBinCallback extends DataFetchCallback<Map<String, Double>> {

        public AuctionAverageLBinCallback() {
            super(LOGGER, URI.create(buildUrl()));
        }

        @Override
        public void completed(Map<String, Double> result) {
            super.completed(result);
            main.setLowestBinAveragesData(result);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("Auction average LBIN data loaded with '{}' entries", result.size());
            }

            if (apiAuctionAverageLBinError) {
                apiAuctionAverageLBinError = false;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiUpdated", "LBIN Averages"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        @Override
        public void failed(Exception ex) {
            if (!apiAuctionAverageLBinError) {
                apiAuctionAverageLBinError = true;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiError", "LBIN Averages"))
                                .withColor(ColorCode.RED.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        private void scheduleNextUpdate() {
            if (updateTask != null) {
                updateTask.cancel();
            }

            // double the lowest bin update interval for averages
            int updateInterval = Feature.ITEM_PRICES_IN_TOOLTIP.getAsNumber(LOWEST_BIN_PRICES_UPDATE_INTERVAL).intValue();
            int delayTicks = (updateInterval + 1) * 20 * 2;

            updateTask = main.getScheduler().scheduleAsyncTask(
                    scheduledTask -> DataUtils.loadOnlineData(new LowestBinAveragesRequest()), delayTicks
            );

            LBinAveragesType type = (LBinAveragesType) Feature.ITEM_PRICES_IN_TOOLTIP.getAsEnum(LBIN_AVERAGES_TYPE);
            LOGGER.debug("Next LBIN averages data ({}) update scheduled in {} ticks", type.getUrlPath(), delayTicks);
        }
    }

}