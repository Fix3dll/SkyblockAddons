package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.PetItem;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class PetItemsRequest extends RemoteFileRequest<Map<String, PetItem>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/petItems.json";

    public PetItemsRequest() {
        super(
                PATH,
                new TypeToken<Map<String, PetItem>>() {}.getType(),
                new PetItemsCallback()
        );
    }

    public static class PetItemsCallback extends DataFetchCallback<Map<String, PetItem>> {

        public PetItemsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, PetItem> result) {
            super.completed(result);
            PetManager.setPetItems(Map.copyOf(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR)));
        }

    }

}