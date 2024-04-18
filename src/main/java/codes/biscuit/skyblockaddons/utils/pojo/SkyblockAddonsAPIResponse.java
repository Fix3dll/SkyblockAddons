package codes.biscuit.skyblockaddons.utils.pojo;

import com.google.gson.JsonObject;
import lombok.Data;

@Data
@Deprecated
public class SkyblockAddonsAPIResponse {

    private boolean success;
    private JsonObject response;
}
