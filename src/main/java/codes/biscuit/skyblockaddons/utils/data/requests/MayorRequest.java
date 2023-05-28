package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

public class MayorRequest extends RemoteFileRequest<JsonObject> {
    public MayorRequest() {
            super("https://api.hypixel.net/resources/skyblock/election"
                    , new JSONResponseHandler<>(JsonObject.class)
                    , false
                    , true);
        }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SkyblockAddons main = SkyblockAddons.getInstance();
        String mayorName = getResult().get("mayor").getAsJsonObject().get("name").getAsString();
        main.getUtils().setMayor(
                mayorName == null ? "" : mayorName
        );
    }
}
