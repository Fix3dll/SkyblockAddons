package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.core.Rarity;
import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DragonsSince {

    SUPERIOR(Rarity.LEGENDARY),
    ASPECT_OF_THE_DRAGONS(Rarity.LEGENDARY),
    ENDER_DRAGON_PET(Rarity.LEGENDARY);

    @Getter private Rarity itemRarity;

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." +  CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}
