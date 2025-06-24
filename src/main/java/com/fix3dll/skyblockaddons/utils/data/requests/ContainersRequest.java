package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class ContainersRequest extends RemoteFileRequest<Object2ObjectOpenHashMap<String, ContainerData>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public ContainersRequest() {
        super(
                "skyblock/containers.json",
                new JSONResponseHandler<>(new TypeToken<Object2ObjectOpenHashMap<String, ContainerData>>() {}.getType()),
                new ContainerCallback(getCDNBaseURL() + "skyblock/containers.json")
        );
    }

    public static class ContainerCallback extends DataFetchCallback<Object2ObjectOpenHashMap<String, ContainerData>> {

        public ContainerCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(Object2ObjectOpenHashMap<String, ContainerData> result) {
            super.completed(result);
            ItemUtils.setContainers(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
