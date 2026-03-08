package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
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

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LOWEST_BIN_PRICES_UPDATE_INTERVAL;

public class LowestBinRequest extends RemoteFileRequest<Map<String, Double>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String URL = "https://moulberry.codes/lowestbin.json.gz";

    private static boolean apiLowestBinError = false;
    private static ScheduledTask updateTask;

    public LowestBinRequest() {
        super(
                URL,
                new TypeToken<Map<String, Double>>() {}.getType(),
                new LowestBinCallback(),
                false,
                true
        );
    }

    /**
     * Cancels the scheduled update task.
     * To restart the update cycle, call {@code DataUtils.loadOnlineData(new LowestBinRequest())}.
     */
    public static void cancelUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            apiLowestBinError = false;
            LOGGER.info("Lowest BIN update task cancelled.");
        }
    }

    private static class LowestBinCallback extends DataFetchCallback<Map<String, Double>> {

        public LowestBinCallback() {
            super(LOGGER, URI.create(URL));
        }

        @Override
        public void completed(Map<String, Double> result) {
            super.completed(result);
            main.setLowestBinData(result);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("Lowest BIN data loaded with '{}' entries", result.size());
            }

            if (apiLowestBinError) {
                apiLowestBinError = false;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiUpdated", "Lowest BIN"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        @Override
        public void failed(Throwable ex) {
            super.failed(ex);

            if (!apiLowestBinError) {
                apiLowestBinError = true;
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiError", "Lowest BIN"))
                                .withColor(ColorCode.RED.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        private void scheduleNextUpdate() {
            if (updateTask != null) {
                updateTask.cancel();
            }

            // lowestbin data approximately updates every 1 minute; +1s buffer
            int updateInterval = Feature.ITEM_PRICES_IN_TOOLTIP.getAsNumber(LOWEST_BIN_PRICES_UPDATE_INTERVAL).intValue();
            int delayTicks = (updateInterval + 1) * 20;

            updateTask = main.getScheduler().scheduleAsyncTask(
                    scheduledTask -> DataUtils.loadOnlineData(new LowestBinRequest()), delayTicks
            );

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("Next bazaar update scheduled in {} ticks", delayTicks);
            }
        }
    }

}