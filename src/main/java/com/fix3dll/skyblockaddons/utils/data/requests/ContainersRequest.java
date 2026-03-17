package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class ContainersRequest extends RemoteFileRequest<Map<String, ContainerData>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/containers.json";

    public ContainersRequest() {
        super(
                PATH,
                new TypeToken<Map<String, ContainerData>>() {}.getType(),
                new ContainerCallback()
        );
    }

    public static class ContainerCallback extends DataFetchCallback<Map<String, ContainerData>> {

        public ContainerCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, ContainerData> result) {
            super.completed(result);
            ItemUtils.setContainers(Map.copyOf(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR)));
        }

    }

}