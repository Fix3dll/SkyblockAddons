package codes.biscuit.skyblockaddons.utils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class LocationData {
    @SerializedName("map")
    public String map = "null";
    @SerializedName("zones")
    public List<String> zones = Collections.emptyList();
}
