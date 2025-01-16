package codes.biscuit.skyblockaddons.features.enchants;

import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonCycling;
import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;

/**
 * Statuses that are shown on the Discord RPC feature
 */
public enum EnchantLayout implements ButtonCycling.SelectItem, RegistrableEnum {
    NORMAL("enchantLayout.titleNormal", "enchantLayout.descriptionNormal"),
    COMPRESS("enchantLayout.titleCompress", "enchantLayout.descriptionCompress"),
    EXPAND("enchantLayout.titleExpand", "enchantLayout.descriptionExpand");

    private final String title;
    private final String description;

    EnchantLayout(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return Translations.getMessage(title);
    }

    @Override
    public String getDescription() {
        return Translations.getMessage(description);
    }
}