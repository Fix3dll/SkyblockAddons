package codes.biscuit.skyblockaddons.utils.pojo;

import codes.biscuit.skyblockaddons.core.Rarity;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class PetInfo {
    @SerializedName("type")
    private String petSkyblockId;
    @SerializedName("active")
    private boolean active;
    @SerializedName("exp")
    private double exp;
    @SerializedName("tier")
    private Rarity petRarity;
    @SerializedName("hideInfo")
    private boolean hideInfo;
    @SerializedName("heldItem")
    @Setter private String heldItemId;
    @SerializedName("candyUsed")
    private int candyUsed;
    @SerializedName("uuid")
    private UUID uuid;
    @SerializedName("uniqueId")
    private UUID uniqueId;
    @SerializedName("hideRightClick")
    private boolean hideRightClick;
    @SerializedName("noMove")
    private boolean noMove;

    public boolean equals(PetInfo other) {
        if (other == null) return false;
        if (this.active != other.active) return false;
        if (!Objects.equals(this.petSkyblockId, other.petSkyblockId)) return false;
        if (this.petRarity != other.petRarity) return false;
        if (this.exp != other.exp) return false;
        if (!Objects.equals(this.heldItemId, other.heldItemId)) return false;
        if (this.candyUsed != other.candyUsed) return false;

        return Objects.equals(this.uniqueId, other.uniqueId); // the last castle
    }
}
