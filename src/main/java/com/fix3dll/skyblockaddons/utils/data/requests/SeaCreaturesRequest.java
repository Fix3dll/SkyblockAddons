package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.seacreatures.SeaCreature;
import com.fix3dll.skyblockaddons.core.seacreatures.SeaCreatureManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SeaCreaturesRequest extends RemoteFileRequest<Map<String, SeaCreature>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/seaCreatures.json";

    public SeaCreaturesRequest() {
        super(
                PATH,
                new TypeToken<Map<String, SeaCreature>>() {}.getType(),
                new SeaCreaturesCallback()
        );
    }

    public static class SeaCreaturesCallback extends DataFetchCallback<Map<String, SeaCreature>> {

        public SeaCreaturesCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(Map<String, SeaCreature> result) {
            super.completed(result);
            SeaCreatureManager.getInstance().setSeaCreatures(
                    Map.copyOf(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR))
            );
        }

    }

}