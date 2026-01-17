package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.enchants.EnchantManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.EnchantmentsData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class EnchantmentsRequest extends RemoteFileRequest<EnchantmentsData> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/enchants.json";

    public EnchantmentsRequest() {
        super(
                PATH,
                new TypeToken<EnchantmentsData>() {}.getType(),
                new EnchantmentsCallback()
        );
    }

    public static class EnchantmentsCallback extends DataFetchCallback<EnchantmentsData> {

        public EnchantmentsCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(EnchantmentsData result) {
            super.completed(result);
            EnchantManager.setEnchants(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }

    }

}