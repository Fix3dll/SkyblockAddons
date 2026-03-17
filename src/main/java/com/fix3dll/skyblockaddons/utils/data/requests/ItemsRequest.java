package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ItemsData;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Date;

public class ItemsRequest extends RemoteFileRequest<ItemsData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final String PATH = "https://api.hypixel.net/v2/resources/skyblock/items";

    public ItemsRequest() {
        super(
                PATH,
                ItemsData.class,
                new ItemsCallback(),
                false,
                true
        );
    }

    private static class ItemsCallback extends DataFetchCallback<ItemsData> {

        public ItemsCallback() {
            super(LOGGER, URI.create(PATH));
        }

        @Override
        public void completed(ItemsData result) {
            super.completed(result);
            main.setItemsData(result);

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                LOGGER.info("lastUpdated: {}, items: {}", new Date(result.getLastUpdated()), result.getById().size());
            }
        }
    }

}