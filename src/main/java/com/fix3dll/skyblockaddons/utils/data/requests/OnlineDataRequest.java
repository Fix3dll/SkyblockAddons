package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class OnlineDataRequest extends RemoteFileRequest<OnlineData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblockaddons/data.json";

    public OnlineDataRequest() {
        super(
                "skyblockaddons/data.json",
                OnlineData.class,
                new OnlineDataCallback()
//                , false
//                , true
        );
    }

    public static class OnlineDataCallback extends DataFetchCallback<OnlineData> {

        public OnlineDataCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(OnlineData result) {
            super.completed(result);
            SkyblockAddons main = SkyblockAddons.getInstance();
            main.setOnlineData(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
            main.getUpdater().checkForUpdate();
        }

    }

}