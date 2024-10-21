package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Optional;

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
            String jerryMayorName = Optional.ofNullable(result)
                    .map(r -> r.get("mayor")).map(JsonElement::getAsJsonObject)
                    .map(jsonObject -> jsonObject.get("name")).map(JsonElement::getAsString)
                    .orElse(null);
            String oldJerryMayor = main.getUtils().getJerryMayor();

            main.getUtils().setJerryMayor(jerryMayorName == null ? "Fix3dll" : jerryMayorName);

            if (jerryMayorName != null) {
                LOGGER.info("Jerry's Perkpocalypse mayor switched to {}", jerryMayorName);
                main.getUtils().setJerryMayorUpdateTime(result.get("nextSwitch").getAsLong());

                if (main.getUtils().isOnSkyblock() && !oldJerryMayor.equals(jerryMayorName)) {
                    main.getUtils().sendMessage(
                            EnumChatFormatting.GREEN + "Jerry's Perkpocalypse mayor switched to " + jerryMayorName
                    );
                }
            }
        }
    }
}
