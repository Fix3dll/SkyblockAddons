package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.CompactorItem;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class CompactorItemsRequest extends RemoteFileRequest<Object2ObjectOpenHashMap<String, CompactorItem>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public CompactorItemsRequest() {
        super(
                "skyblock/compactorItems.json",
                new JSONResponseHandler<>(new TypeToken<Object2ObjectOpenHashMap<String, CompactorItem>>() {}.getType()),
                new CompactorItemsCallback(getCDNBaseURL() + "skyblock/compactorItems.json")
        );
    }

    public static class CompactorItemsCallback extends DataFetchCallback<Object2ObjectOpenHashMap<String, CompactorItem>> {

        public CompactorItemsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(Object2ObjectOpenHashMap<String, CompactorItem> result) {
            super.completed(result);
            Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR).forEach((skyblockId, compactorItem) ->
                    ItemUtils.setItemStackSkyblockID(compactorItem.getItemStack(), skyblockId)
            );
            ItemUtils.setCompactorItems(result);
        }
    }
}
