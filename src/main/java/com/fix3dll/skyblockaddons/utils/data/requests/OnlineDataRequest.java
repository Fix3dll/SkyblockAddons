package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class OnlineDataRequest extends RemoteFileRequest<OnlineData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public OnlineDataRequest() {
        super(
                "skyblockaddons/data.json",
                new JSONResponseHandler<>(OnlineData.class),
                new OnlineDataCallback(getCDNBaseURL() + "skyblockaddons/data.json")
//                , false
//                , true
        );
    }

    public static class OnlineDataCallback extends DataFetchCallback<OnlineData> {

        public OnlineDataCallback(String path) {
            super(LOGGER, URI.create(path));
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