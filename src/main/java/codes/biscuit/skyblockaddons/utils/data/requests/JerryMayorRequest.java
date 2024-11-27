package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.MayorJerryData;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Optional;

public class JerryMayorRequest extends RemoteFileRequest<MayorJerryData> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public JerryMayorRequest() {
        super(
                "https://api.skytils.gg/api/mayor/jerry"
                , new JSONResponseHandler<>(MayorJerryData.class),
                new JerryMayorCallback("https://api.skytils.gg/api/mayor/jerry")
                , false
                , true
        );
    }

    public static class JerryMayorCallback extends DataFetchCallback<MayorJerryData> {

        public JerryMayorCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(MayorJerryData result) {
            super.completed(result);
            SkyblockAddons main = SkyblockAddons.getInstance();
            MayorJerryData oldData = main.getMayorJerryData();
            main.setMayorJerryData(result);

            String jerryMayorName = Optional.ofNullable(result)
                    .map(MayorJerryData::getMayor)
                    .map(MayorJerryData.Mayor::getName)
                    .orElse(null);

            if (jerryMayorName != null) {
                LOGGER.info("Jerry's Perkpocalypse mayor switched to {}", jerryMayorName);

                if (main.getUtils().isOnSkyblock() && !jerryMayorName.equals(oldData.getMayor().getName())) {
                    main.getUtils().sendMessage(
                            EnumChatFormatting.GREEN + "Jerry's Perkpocalypse mayor switched to " + jerryMayorName
                    );
                }
            }
        }
    }
}
