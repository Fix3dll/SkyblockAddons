package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class CooldownsRequest extends RemoteFileRequest<HashMap<String, Integer>> {
    private static final Logger LOGGER = LogManager.getLogger();

    public CooldownsRequest() {
        super(
                "skyblock/cooldowns.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, Integer>>() {}.getType()),
                new CooldownsCallback(getCDNBaseURL() + "skyblock/cooldowns.json")
        );
    }

    public static class CooldownsCallback extends DataFetchCallback<HashMap<String, Integer>> {

        public CooldownsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, Integer> result) {
            super.completed(result);

            Object2IntOpenHashMap<String> cooldowns = new Object2IntOpenHashMap<>();
            cooldowns.putAll(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));

            CooldownManager.setItemCooldowns(cooldowns);
        }
    }
}
