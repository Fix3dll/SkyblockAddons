package com.fix3dll.skyblockaddons.utils.data.requests;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.SkillXpManager;
import com.fix3dll.skyblockaddons.utils.data.DataConstants;
import com.fix3dll.skyblockaddons.utils.data.DataFetchCallback;
import com.fix3dll.skyblockaddons.utils.data.RemoteFileRequest;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class SkillXpRequest extends RemoteFileRequest<SkillXpManager.JsonInput> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final String PATH = "skyblock/skillXp.json";

    public SkillXpRequest() {
        super(
                PATH,
                SkillXpManager.JsonInput.class,
                new SkillXpCallback()
        );
    }

    public static class SkillXpCallback extends DataFetchCallback<SkillXpManager.JsonInput> {

        public SkillXpCallback() {
            super(LOGGER, URI.create(DataConstants.CDN_BASE_URL + PATH));
        }

        @Override
        public void completed(SkillXpManager.JsonInput result) {
            super.completed(result);
            SkillXpManager.initialize(
                    Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR)
            );
        }

    }

}