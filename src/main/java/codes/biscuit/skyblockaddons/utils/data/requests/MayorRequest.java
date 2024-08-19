package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.pojo.ElectionData;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class MayorRequest extends RemoteFileRequest<ElectionData> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public MayorRequest() {
            super("https://api.hypixel.net/v2/resources/skyblock/election"
                    , new JSONResponseHandler<>(ElectionData.class)
                    , false
                    , true);
        }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        String mayorName = getResult().getMayor().getName();
        main.getUtils().setMayor(mayorName == null ? "Fix3dll" : mayorName);

        ElectionData.Mayor.Minister minister = getResult().getMayor().getMinister();
        if (minister != null && minister.getPerk() != null) {
            main.getUtils().setMinisterAndPerk(
                    new Pair<>(minister.getName(), minister.getPerk().getName())
            );
        }

        // Jerry's Perkpocalypse mayor updater
        main.getNewScheduler().runAsync(new SkyblockRunnable() {
            @Override
            public void run() {
                if (main.getUtils().getMayor().startsWith("Jerry") && System.currentTimeMillis() > main.getUtils().getJerryMayorUpdateTime()) {
                    DataUtils.loadOnlineData(new JerryMayorRequest());

                    String name = main.getUtils().getJerryMayor();
                    LOGGER.info("Jerry's Perkpocalypse mayor switched to {}", name);
                    if (main.getUtils().isOnSkyblock() && !main.getUtils().getJerryMayor().equals(name))
                        main.getUtils().sendMessage(
                                EnumChatFormatting.GREEN + "Jerry's Perkpocalypse mayor switched to " + name,
                                true
                        );
                }
            }
        }, 0, 60 * 20);
    }
}
