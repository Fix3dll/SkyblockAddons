package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class SlayerLocationsRequest extends RemoteFileRequest<HashMap<String, Set<String>>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public SlayerLocationsRequest() {
        super(
                "skyblock/slayerLocations.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, Set<String>>>() {}.getType()),
                new SlayerLocationsCallback(getCDNBaseURL() + "skyblock/slayerLocations.json")
        );
    }

    public static class SlayerLocationsCallback extends DataFetchCallback<HashMap<String, Set<String>>> {

        public SlayerLocationsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, Set<String>> result) {
            super.completed(result);
            LocationUtils.setSlayerLocations(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
