package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class ContainersRequest extends RemoteFileRequest<HashMap<String, ContainerData>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public ContainersRequest() {
        super(
                "skyblock/containers.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, ContainerData>>() {}.getType()),
                new ContainerCallback(getCDNBaseURL() + "skyblock/containers.json")
        );
    }

    public static class ContainerCallback extends DataFetchCallback<HashMap<String, ContainerData>> {

        public ContainerCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, ContainerData> result) {
            super.completed(result);
            ItemUtils.setContainers(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
