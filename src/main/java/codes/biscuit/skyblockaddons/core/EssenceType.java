package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;

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

    private String niceName;
    private ResourceLocation resourceLocation;

    EssenceType() {
        niceName = WordUtils.capitalizeFully(this.name());
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
