package codes.biscuit.skyblockaddons.utils.data.skyblockdata;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class MayorJerryData {

    @SerializedName("nextSwitch")
    private Long nextSwitch;
    @SerializedName("mayor")
    private Mayor mayor;

    public boolean hasMayorAndActive() {
        return mayor != null && nextSwitch >= System.currentTimeMillis();
    }

    @Getter
    public static class Mayor {
        @SerializedName("name")
        private String name = "Fix3dll";
        @SerializedName("perks")
        private List<Perk> perks = Collections.emptyList();
    }

    @Getter
    public static class Perk {
        @SerializedName("name")
        private String name;
        @SerializedName("description")
        private String description;

    }
}