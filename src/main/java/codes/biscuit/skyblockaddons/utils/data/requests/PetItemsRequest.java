package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.features.PetManager;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetItem;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PetItemsRequest extends RemoteFileRequest<HashMap<String, PetItem>> {
    public PetItemsRequest() {
        super(
                "skyblock/petItems.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, PetItem>>() {}.getType())
        );
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        PetManager.setPetItems(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
