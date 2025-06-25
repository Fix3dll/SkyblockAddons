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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;

public class MayorRequest extends RemoteFileRequest<ElectionData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    /** New mayor name according to latest election results from chat */
    private static String newMayorName = "";
    private static ScheduledTask updateTask;
    private static ScheduledTask jerryMayorTask;

    public MayorRequest() {
        this("");
    }

    /**
     * This constructor is used to update the API data after the mayor election is completed and the new mayor is
     * announced via chat.
     * @param newMayorName according to the latest election results in the chat, the name of the new mayor
     */
    public MayorRequest(String newMayorName) {
        super(
                "https://api.hypixel.net/v2/resources/skyblock/election",
                new JSONResponseHandler<>(ElectionData.class),
                new MayorCallback("https://api.hypixel.net/v2/resources/skyblock/election"),
                false,
                true
        );
        MayorRequest.newMayorName = newMayorName;
    }

    private static class MayorCallback extends DataFetchCallback<ElectionData> {

        public MayorCallback(String path) {
            super(LOGGER, URI.create(path));
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
            if (newMayorName.isEmpty() || newMayorName.equals(mayorName)) {
                main.getUtils().setMayor(mayorName == null ? "Fix3dll" : mayorName);
            }

            // Jerry's Perkpocalypse mayor updater
            if (isMayorJerry && jerryMayorTask == null) {
                jerryMayorTask = scheduleJerryMayorTask();
            } else if (!isMayorJerry && jerryMayorTask != null) {
                jerryMayorTask.cancel();
                jerryMayorTask = null;
            }

            // If newMayorName is not equals to new API data's mayor field,
            // schedule new update based on next update time of API data.
            if (!newMayorName.isEmpty() && !newMayorName.equals(mayorName) && updateTask == null) {
                updateTask = scheduleUpdateTask(newMayorName);
                LOGGER.info("Update task scheduled.");
            } else if (newMayorName.equals(mayorName)) {
                if (updateTask != null) {
                    updateTask.cancel();
                    updateTask = null;
                    LOGGER.info("Scheduled update task completed.");
                }
                newMayorName = "";
            }
        }

        private ScheduledTask scheduleUpdateTask(String expectedMayorName) {
            // election endpoint is updated every 5 minutes (+5 minutes and bonus 3 seconds)
            long nextUpdateTime = main.getElectionData().getLastUpdated() + 303000L;
            int delayTick = (int) (nextUpdateTime - System.currentTimeMillis()) / 50;

            return main.getScheduler().scheduleAsyncTask(
                    scheduledTask -> DataUtils.loadOnlineData(new MayorRequest(expectedMayorName)),
                    delayTick
            );
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
                    updateText.withStyle(style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/calendar")
                    ).withHoverEvent(
                            new HoverEvent.ShowText(Component.literal("§7/calendar"))
                    ));
                    Utils.sendMessage(updateText, true);
                }
            }, 0, 3 * 20);
        }

    }
}