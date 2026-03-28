package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.CompactorItem;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class CompactorItemsRequest extends RemoteFileRequest<Map<String, CompactorItem>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/compactorItems.json";

    public CompactorItemsRequest() {
        super(
                PATH,
                new TypeToken<Map<String, CompactorItem>>() {}.getType(),
                new CompactorItemsCallback()
        );
    }

    public static class CompactorItemsCallback extends DataFetchCallback<Map<String, CompactorItem>> {

        public CompactorItemsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, CompactorItem> result) {
            super.completed(result);
            Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR).forEach((skyblockId, compactorItem) ->
                    compactorItem.setSkyblockId(skyblockId)
            );
            ItemUtils.setCompactorItems(Map.copyOf(result));
        }

    }

}