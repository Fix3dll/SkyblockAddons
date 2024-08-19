package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.pojo.LocationData;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LocationsRequest extends RemoteFileRequest<HashMap<String, LocationData>> {

    public LocationsRequest() {
        super(
                "skyblock/locations.json"
                , new JSONResponseHandler<>(new TypeToken<HashMap<String, LocationData>>() {}.getType())
        );
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        HashMap<String, LocationData> result = Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR);

        for (Map.Entry<String, LocationData> entry : result.entrySet()) {
            for (Island island : Island.values()) {
                if (island.getMode().equalsIgnoreCase(entry.getKey())) {
                    island.setLocationData(entry.getValue());
                }
            }
        }
    }
}
