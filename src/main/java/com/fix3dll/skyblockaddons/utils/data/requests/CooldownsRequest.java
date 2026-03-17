package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.JSONResponseHandler;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class CooldownsRequest extends RemoteFileRequest<Object2DoubleMap<String>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/cooldowns.json";

    public CooldownsRequest() {
        super(
                PATH,
                new JSONResponseHandler<>(new TypeToken<Object2DoubleMap<String>>() {}.getType()),
                new CooldownsCallback()
        );
    }

    public static class CooldownsCallback extends DataFetchCallback<Object2DoubleMap<String>> {

        public CooldownsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Object2DoubleMap<String> result) {
            super.completed(result);
            Object2IntOpenHashMap<String> cooldowns = new Object2IntOpenHashMap<>(
                    Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR).size()
            );
            for (Object2DoubleMap.Entry<String> entry : result.object2DoubleEntrySet()) {
                // direct primitive cast from double to int
                cooldowns.put(entry.getKey(), (int) entry.getDoubleValue());
            }
            CooldownManager.setItemCooldowns(Object2IntMaps.unmodifiable(cooldowns));
        }

    }

}