package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class CompactorItemsRequest extends RemoteFileRequest<HashMap<String, CompactorItem>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public CompactorItemsRequest() {
        super(
                "skyblock/compactorItems.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, CompactorItem>>() {}.getType()),
                new CompactorItemsCallback(getCDNBaseURL() + "skyblock/compactorItems.json")
        );
    }

    public static class CompactorItemsCallback extends DataFetchCallback<HashMap<String, CompactorItem>> {

        public CompactorItemsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, CompactorItem> result) {
            super.completed(result);
            ItemUtils.setCompactorItems(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
