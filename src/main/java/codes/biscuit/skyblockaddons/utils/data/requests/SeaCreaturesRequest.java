package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreature;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SeaCreaturesRequest extends RemoteFileRequest<Map<String, SeaCreature>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public SeaCreaturesRequest() {
        super(
                "skyblock/seaCreatures.json",
                new JSONResponseHandler<>(new TypeToken<Map<String, SeaCreature>>() {}.getType()),
                new SeaCreaturesCallback(getCDNBaseURL() + "skyblock/seaCreatures.json")
        );
    }

    public static class SeaCreaturesCallback extends DataFetchCallback<Map<String, SeaCreature>> {

        public SeaCreaturesCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(Map<String, SeaCreature> result) {
            super.completed(result);
            SeaCreatureManager.getInstance().setSeaCreatures(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
