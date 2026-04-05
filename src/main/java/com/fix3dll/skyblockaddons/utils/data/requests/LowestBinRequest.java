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
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.LOWEST_BIN_PRICES_UPDATE_INTERVAL;

public class LowestBinRequest extends RemoteFileRequest<Object2DoubleMap<String>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String URL = "https://api.eliteskyblock.com/resources/auctions/neu";

    private static final AtomicBoolean apiLowestBinError = new AtomicBoolean(false);
    private static final AtomicReference<ScheduledTask> updateTaskRef = new AtomicReference<>();

    public LowestBinRequest() {
        super(
                URL,
                new TypeToken<Object2DoubleMap<String>>() {}.getType(),
                new LowestBinCallback(),
                false,
                true
        );
    }

    /**
     * Starts or stops the scheduled polling cycle for the Lowest BIN API.
     * <p>If {@code active} is {@code true}, an initial request is triggered
     * only when no scheduled update task currently exists. This safely prevents
     * overlapping background tasks and duplicate network requests.
     * <p>If {@code active} is {@code false}, any ongoing scheduled polling task is
     * safely cancelled and the API error state is reset.
     * @param active {@code true} to start the background data fetching cycle, {@code false} to stop it
     */
    public static void setActive(boolean active) {
        if (active) {
            if (updateTaskRef.get() == null) {
                DataUtils.loadOnlineData(new LowestBinRequest());
            }
            return;
        }

        ScheduledTask oldTask = updateTaskRef.getAndSet(null);
        if (oldTask != null) {
            oldTask.cancel();
            LOGGER.info("Lowest BIN update task cancelled.");
        }

        apiLowestBinError.set(false);
    }

    private static class LowestBinCallback extends DataFetchCallback<Object2DoubleMap<String>> {

        public LowestBinCallback() {
            super(LOGGER, URI.create(URL));
        }

        @Override
        public void completed(Object2DoubleMap<String> result) {
            super.completed(result);
            main.setLowestBinData(Object2DoubleMaps.unmodifiable(result));

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("Lowest BIN data loaded with '{}' entries", result.size());
            }

            if (apiLowestBinError.compareAndSet(true, false)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiUpdated", "Lowest BIN"))
                                .withColor(ColorCode.GREEN.getColor())
                ));
            }

            scheduleNextUpdate();
        }

        @Override
        public void failed(Throwable ex) {
            if (apiLowestBinError.compareAndSet(false, true)) {
                Minecraft.getInstance().execute(() -> Utils.sendMessage(
                        Component.literal(Translations.getMessage("messages.itemPricesInTooltip.apiError", "Lowest BIN"))
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

            int delayTicks = (updateInterval + 1) * 20;

            ScheduledTask newTask = main.getScheduler().scheduleAsyncTask(scheduledTask -> {
                try {
                    DataUtils.loadOnlineData(new LowestBinRequest());
                } finally {
                    updateTaskRef.compareAndSet(scheduledTask, null);
                }
            }, delayTicks);

            ScheduledTask oldTask = updateTaskRef.getAndSet(newTask);
            if (oldTask != null) {
                oldTask.cancel();
            }

            LOGGER.debug("Next Lowest BIN update scheduled in {} ticks", delayTicks);
        }
    }

}