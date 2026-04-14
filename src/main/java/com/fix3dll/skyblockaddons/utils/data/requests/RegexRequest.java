package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Regex;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexRequest extends RemoteFileRequest<Map<String, Pattern>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/regex.json";

    public RegexRequest() {
        super(
                PATH,
                new TypeToken<Map<String, Pattern>>() {}.getType(),
                new PatternsCallback()
        );
    }

    public static class PatternsCallback extends DataFetchCallback<Map<String, Pattern>> {

        public PatternsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, Pattern> result) {
            super.completed(result);
            for (Map.Entry<String, Pattern> entry : result.entrySet()) {
                String key = entry.getKey();
                Pattern pattern = entry.getValue();
                try {
                    Regex.valueOf(entry.getKey()).setPattern(entry.getValue());
                } catch (IllegalArgumentException e) {
                    try {
                        InventoryType.valueOf(key).setInventoryPattern(pattern);
                    } catch (IllegalArgumentException e2) {
                        LOGGER.warn("Patterns data contains unrecognized key '{}', skipping.", key);
                    }
                }
            }
        }

    }

}