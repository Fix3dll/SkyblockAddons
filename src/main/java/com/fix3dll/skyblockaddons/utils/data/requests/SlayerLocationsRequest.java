package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class SlayerLocationsRequest extends RemoteFileRequest<HashMap<String, Set<String>>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/slayerLocations.json";

    public SlayerLocationsRequest() {
        super(
                PATH,
                new TypeToken<HashMap<String, Set<String>>>() {}.getType(),
                new SlayerLocationsCallback()
        );
    }

    public static class SlayerLocationsCallback extends DataFetchCallback<HashMap<String, Set<String>>> {

        public SlayerLocationsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(HashMap<String, Set<String>> result) {
            super.completed(result);
            LocationUtils.setSlayerLocations(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }

    }

}