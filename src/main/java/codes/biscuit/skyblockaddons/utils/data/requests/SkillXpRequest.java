package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.SkillXpManager;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class SkillXpRequest extends RemoteFileRequest<SkillXpManager.JsonInput> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public SkillXpRequest() {
        super(
                "skyblock/skillXp.json",
                new JSONResponseHandler<>(SkillXpManager.JsonInput.class),
                new SkillXpCallback(getCDNBaseURL() + "skyblock/skillXp.json")
        );
    }

    public static class SkillXpCallback extends DataFetchCallback<SkillXpManager.JsonInput> {

        public SkillXpCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(SkillXpManager.JsonInput result) {
            super.completed(result);
            SkyblockAddons.getInstance().getSkillXpManager().initialize(
                    Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR)
            );
        }
    }
}
