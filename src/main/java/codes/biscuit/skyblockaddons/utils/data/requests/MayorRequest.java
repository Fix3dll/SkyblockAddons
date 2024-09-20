package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.pojo.ElectionData;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;

public class MayorRequest extends RemoteFileRequest<ElectionData> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static boolean forceUpdate;
    private static ScheduledTask jerryMayorTask;

    public MayorRequest(boolean forceUpdate) {
        super(
                "https://api.hypixel.net/v2/resources/skyblock/election",
                new JSONResponseHandler<>(ElectionData.class),
                new MayorCallback("https://api.hypixel.net/v2/resources/skyblock/election"),
                false,
                true
        );
        MayorRequest.forceUpdate = forceUpdate;
    }

     private static class MayorCallback extends DataFetchCallback<ElectionData> {

        public MayorCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(ElectionData result) {
            super.completed(result);
            main.setElectionData(result);
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("lastUpdated: {}", new Date(result.getLastUpdated()));
            }

            String oldMayorName = main.getUtils().getMayor();
            String mayorName = result.getMayor().getName();
            main.getUtils().setMayor(mayorName == null ? "Fix3dll" : mayorName);

            ElectionData.Mayor.Minister minister = result.getMayor().getMinister();
            if (minister != null && minister.getPerk() != null) {
                main.getUtils().setMinisterAndPerk(
                        new Pair<>(minister.getName(), minister.getPerk().getName())
                );
            }

            // Jerry's Perkpocalypse mayor updater
            if ("Jerry".equalsIgnoreCase(mayorName) && jerryMayorTask == null) {
                jerryMayorTask = scheduleJerryMayorTask();
            } else if (jerryMayorTask != null) {
                jerryMayorTask.cancel();
                jerryMayorTask = null;
            }

            if (MayorRequest.forceUpdate) {
                // If ElectionData is not updated
                // TODO could be more reliable?
                boolean isUpdated = !oldMayorName.equals(mayorName);
                if (isUpdated) {
                    MayorRequest.forceUpdate = false;
                } else {
                    scheduleUpdateTask();
                }
            }
        }

        private void scheduleUpdateTask() {
            // election endpoint is updated every 5 minutes (+5 minutes and bonus 5 seconds)
            long nextUpdateTime = main.getElectionData().getLastUpdated() + 305000L;
            int delayTick = (int) (nextUpdateTime - System.currentTimeMillis()) / 50;

            main.getNewScheduler().runAsync(new SkyblockRunnable() {
                @Override
                public void run() {
                    DataUtils.loadOnlineData(new MayorRequest(true));
                }
            }, delayTick);
        }

        private ScheduledTask scheduleJerryMayorTask() {
            if (!main.getUtils().getMayor().startsWith("Jerry")) return null;

            return main.getNewScheduler().runAsync(new SkyblockRunnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() > main.getUtils().getJerryMayorUpdateTime()) {
                        if (main.getUtils().isOnSkyblock()) {
                            DataUtils.loadOnlineData(new JerryMayorRequest());
                        }
                    }
                }
            }, 0, 60 * 20);
        }

    }
}