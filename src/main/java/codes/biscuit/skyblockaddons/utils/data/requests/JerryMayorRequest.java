package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

public class JerryMayorRequest extends RemoteFileRequest<JsonObject> {
    public JerryMayorRequest() {
        super("https://api.skytils.gg/api/mayor/jerry"
                , new JSONResponseHandler<>(JsonObject.class)
                , false
                , true);
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SkyblockAddons main = SkyblockAddons.getInstance();
        String jerryMayorName = getResult().get("mayor").getAsJsonObject().get("name").getAsString();

        main.getUtils().setJerryMayor(jerryMayorName == null ? "Fix3dll" : jerryMayorName);

        if (jerryMayorName != null)
            main.getUtils().setJerryMayorUpdateTime(getResult().get("nextSwitch").getAsLong());
    }
}
