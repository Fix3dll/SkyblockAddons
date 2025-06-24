package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.PetItem;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class PetItemsRequest extends RemoteFileRequest<HashMap<String, PetItem>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public PetItemsRequest() {
        super(
                "skyblock/petItems.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, PetItem>>() {}.getType()),
                new PetItemsCallback(getCDNBaseURL() + "skyblock/petItems.json")
        );
    }

    public static class PetItemsCallback extends DataFetchCallback<HashMap<String, PetItem>> {

        public PetItemsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, PetItem> result) {
            super.completed(result);
            PetManager.setPetItems(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
