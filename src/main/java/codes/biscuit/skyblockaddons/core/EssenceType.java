package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

@Getter
public enum EssenceType {
    CRIMSON,
    DIAMOND,
    DRAGON,
    GOLD,
    ICE,
    SPIDER,
    UNDEAD,
    WITHER;

    private final String niceName;
    private final ResourceLocation resourceLocation;

    EssenceType() {
        niceName = this.name().charAt(0) + this.name().substring(1).toLowerCase(Locale.ENGLISH);
        resourceLocation = new ResourceLocation("skyblockaddons", "essences/" + this.name().toLowerCase(Locale.US) + ".png");
    }

    public static EssenceType fromName(String name) {
        for (EssenceType essenceType : EssenceType.values()) {
            if (essenceType.niceName.equals(name)) {
                return essenceType;
            }
        }

        return null;
    }
}
