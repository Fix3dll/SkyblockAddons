package codes.biscuit.skyblockaddons.utils.data.skyblockdata;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Set;

public class LocationData {
    @SerializedName("map")
    public String map = "null";
    @SerializedName("zones")
    public Set<String> zones = Collections.emptySet();
}
