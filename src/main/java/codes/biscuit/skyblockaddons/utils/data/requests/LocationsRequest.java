package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.pojo.LocationData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationsRequest extends RemoteFileRequest<HashMap<String, LocationData>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public LocationsRequest() {
        super(
                "skyblock/locations.json"
                , new JSONResponseHandler<>(new TypeToken<HashMap<String, LocationData>>() {}.getType()),
                new LocationsCallback(getCDNBaseURL() + "skyblock/locations.json")
        );
    }

    public static class LocationsCallback extends DataFetchCallback<HashMap<String, LocationData>> {

        public LocationsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, LocationData> result) {
            super.completed(result);
            HashMap<String, LocationData> locationsMap = Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR);

            for (Map.Entry<String, LocationData> entry : locationsMap.entrySet()) {
                for (Island island : Island.values()) {
                    if (island.getMode().equalsIgnoreCase(entry.getKey())) {
                        island.setLocationData(entry.getValue());
                    }
                }
            }
        }
    }
}
