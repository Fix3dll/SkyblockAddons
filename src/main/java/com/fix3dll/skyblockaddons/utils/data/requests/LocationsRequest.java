package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.LocationData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class LocationsRequest extends RemoteFileRequest<Map<String, LocationData>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/locations.json";

    public LocationsRequest() {
        super(
                PATH,
                new JSONResponseHandler<>(new TypeToken<Map<String, LocationData>>() {}.getType()),
                new LocationsCallback()
        );
    }

    public static class LocationsCallback extends DataFetchCallback<Map<String, LocationData>> {

        public LocationsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, LocationData> result) {
            super.completed(result);
            Map<String, LocationData> locationsMap = Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR);

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