package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class CooldownsRequest extends RemoteFileRequest<HashMap<String, Integer>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/cooldowns.json";

    public CooldownsRequest() {
        super(
                PATH,
                new TypeToken<HashMap<String, Integer>>() {}.getType(),
                new CooldownsCallback()
        );
    }

    public static class CooldownsCallback extends DataFetchCallback<HashMap<String, Integer>> {

        public CooldownsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
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