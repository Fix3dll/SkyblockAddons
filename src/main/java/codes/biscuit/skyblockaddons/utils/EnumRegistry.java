package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.features.enchants.EnchantLayout;
import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.NonNull;

import java.util.HashMap;

public class EnumRegistry {

    public static final HashMap<String, Class<? extends RegistrableEnum>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("ANCHOR_POINT", EnumUtils.AnchorPoint.class);
        REGISTRY.put("BACKPACK_STYLE", EnumUtils.BackpackStyle.class);
        REGISTRY.put("DEPLOYABLE_DISPLAY_STYLE", EnumUtils.DeployableDisplayStyle.class);
        REGISTRY.put("PET_ITEM_STYLE", EnumUtils.PetItemStyle.class);
        REGISTRY.put("TEXT_STYLE", EnumUtils.TextStyle.class);
        REGISTRY.put("CHROMA_MODE", EnumUtils.ChromaMode.class);
        REGISTRY.put("COLOR_CODE", ColorCode.class);
        REGISTRY.put("ENCHANT_LAYOUT", EnchantLayout.class);
        REGISTRY.put("LANGUAGE", Language.class);
        // Exceptions: key FeatureSetting.name(), value Enum.class
        // Discord settings
        REGISTRY.put("DISCORD_RP_STATE", DiscordStatus.class);
        REGISTRY.put("DISCORD_RP_DETAILS", DiscordStatus.class);
        REGISTRY.put("DISCORD_RP_AUTO_MODE", DiscordStatus.class);
        // Enchant colors
        REGISTRY.put("COMMA_ENCHANT_COLOR", ColorCode.class);
        REGISTRY.put("POOR_ENCHANT_COLOR", ColorCode.class);
        REGISTRY.put("GOOD_ENCHANT_COLOR", ColorCode.class);
        REGISTRY.put("GREAT_ENCHANT_COLOR", ColorCode.class);
        REGISTRY.put("PERFECT_ENCHANT_COLOR", ColorCode.class);
    }

    public static RegistrableEnum getEnumValue(String enumType, String enumKey) {
        Class<? extends RegistrableEnum> enumClass = REGISTRY.get(enumType);
        if (enumClass == null) {
            throw new IllegalArgumentException("Unknown enum type: " + enumType);
        }
        try {
            return Enum.valueOf(enumClass.asSubclass(Enum.class), enumKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value for " + enumType + ": " + enumKey);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Class<? extends Enum> getEnumClass(@NonNull String enumName) {
        for (Class<? extends RegistrableEnum> enumClass : REGISTRY.values()) {
            // We assume that we are only traversing enum classes.
            for (RegistrableEnum enumType : enumClass.getEnumConstants()) {
                if (enumName.equals(enumType.name())) {
                    return enumClass.asSubclass(Enum.class);
                }
            }
        }
        return null;
    }
}