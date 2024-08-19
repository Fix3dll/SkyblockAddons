package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SlayerLocationsRequest extends RemoteFileRequest<HashMap<String, List<String>>> {
    public SlayerLocationsRequest() {
        super(
                "skyblock/slayerLocations.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, List<String>>>() {}.getType())
        );
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        LocationUtils.setSlayerLocations(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}