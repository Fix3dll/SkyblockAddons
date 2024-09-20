package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class JerryMayorRequest extends RemoteFileRequest<JsonObject> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public JerryMayorRequest() {
        super(
                "https://api.skytils.gg/api/mayor/jerry"
                , new JSONResponseHandler<>(JsonObject.class),
                new JerryMayorCallback("https://api.skytils.gg/api/mayor/jerry")
                , false
                , true
        );
    }

    public static class JerryMayorCallback extends DataFetchCallback<JsonObject> {

        public JerryMayorCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(JsonObject result) {
            super.completed(result);
            SkyblockAddons main = SkyblockAddons.getInstance();
            String jerryMayorName = result.get("mayor").getAsJsonObject().get("name").getAsString();

            main.getUtils().setJerryMayor(jerryMayorName == null ? "Fix3dll" : jerryMayorName);

            if (jerryMayorName != null) {
                LOGGER.info("Jerry's Perkpocalypse mayor switched to {}", jerryMayorName);
                main.getUtils().setJerryMayorUpdateTime(result.get("nextSwitch").getAsLong());

                if (main.getUtils().isOnSkyblock()) {
                    main.getUtils().sendMessage(
                            EnumChatFormatting.GREEN + "Jerry's Perkpocalypse mayor switched to " + jerryMayorName
                    );
                }
            }
        }
    }
}
