package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

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
    WITHER,
    FOSSIL_DUST;

    private final String niceName;
    private final ResourceLocation resourceLocation;

    EssenceType() {
        niceName = this.name().charAt(0) + this.name().substring(1).toLowerCase(Locale.ENGLISH);
        resourceLocation = SkyblockAddons.resourceLocation("essences/" + this.name().toLowerCase(Locale.US) + ".png");
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
