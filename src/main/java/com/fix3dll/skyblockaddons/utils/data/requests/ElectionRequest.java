package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ElectionData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class ElectionRequest extends RemoteFileRequest<ElectionData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String PATH = "https://api.hypixel.net/v2/resources/skyblock/election";

    private static final AtomicReference<ScheduledTask> updateTaskRef = new AtomicReference<>();
    private static final AtomicReference<ScheduledTask> jerryMayorTaskRef = new AtomicReference<>();

    public ElectionRequest() {
        this("");
    }

    public ElectionRequest(String expectedMayorName) {
        super(
                PATH,
                new JSONResponseHandler<>(ElectionData.class),
                new MayorCallback(expectedMayorName),
                false,
                true
        );
    }

    private static class MayorCallback extends DataFetchCallback<ElectionData> {

        private final String expectedMayorName;

        public MayorCallback(String expectedMayorName) {
            super(LOGGER, URI.create(PATH));
            this.expectedMayorName = expectedMayorName == null ? "" : expectedMayorName;
        }

        @Override
        public void completed(ElectionData result) {
            super.completed(result);
            main.setElectionData(result);
            String mayorName = result.getMayor().getName();
            boolean isMayorJerry = "Jerry".equals(mayorName);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("lastUpdated: {}, mayor: {}", new Date(result.getLastUpdated()), mayorName);
            }

            // If initial request or request completed with expected result
            if (expectedMayorName.isEmpty() || expectedMayorName.equals(mayorName)) {
                main.getUtils().setMayor(mayorName == null ? "Fix3dll" : mayorName);
            }

            if (isMayorJerry) {
                ensureJerryMayorTask();
            } else {
                ScheduledTask oldTask = jerryMayorTaskRef.getAndSet(null);
                if (oldTask != null) {
                    oldTask.cancel();
                }
            }

            if (!expectedMayorName.isEmpty() && !expectedMayorName.equals(mayorName)) {
                rescheduleUpdateTask(expectedMayorName);
            } else if (expectedMayorName.equals(mayorName)) {
                ScheduledTask oldTask = updateTaskRef.getAndSet(null);
                if (oldTask != null) {
                    oldTask.cancel();
                    LOGGER.info("Scheduled election update task cancelled.");
                }
            }
        }

        private void rescheduleUpdateTask(String expectedMayorName) {
            long nextUpdateTime = main.getElectionData().getLastUpdated() + 301000L;
            int delayTicks = (int) Math.max(0, (nextUpdateTime - System.currentTimeMillis()) / 50);
            ScheduledTask newTask = main.getScheduler().scheduleAsyncTask(scheduledTask -> {
                try {
                    DataUtils.loadOnlineData(new ElectionRequest(expectedMayorName));
                } finally {
                    updateTaskRef.compareAndSet(scheduledTask, null);
                }
            }, delayTicks);

            ScheduledTask oldTask = updateTaskRef.getAndSet(newTask);
            if (oldTask != null) {
                oldTask.cancel();
            }

            LOGGER.info("Election update task scheduled.");
        }

        private void ensureJerryMayorTask() {
            if (jerryMayorTaskRef.get() != null) {
                return;
            }

            ScheduledTask newTask = scheduleJerryMayorTask();
            if (!jerryMayorTaskRef.compareAndSet(null, newTask)) {
                newTask.cancel();
            }
        }

        private ScheduledTask scheduleJerryMayorTask() {
            return main.getScheduler().scheduleAsyncTask(scheduledTask -> {
                if (!main.getUtils().isOnSkyblock()) {
                    return;
                } else if (scheduledTask.updatePeriod(300 * 20)) {
                    return;
                }

                if (System.currentTimeMillis() > main.getMayorJerryData().getNextSwitch()) {
                    MutableComponent updateText = Component.literal(
                            ColorCode.RED + Translations.getMessage("messages.perkpocalypseUnknown")
                    );
                    updateText.withStyle(style -> style
                            .withClickEvent(new ClickEvent.RunCommand("/calendar"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7/calendar")))
                    );
                    Minecraft.getInstance().execute(() -> Utils.sendMessage(updateText, true));
                }
            }, 0, 3 * 20);
        }

    }

}