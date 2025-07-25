package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

@Getter
public enum Language implements RegistrableEnum {

    // listed by popularity
    ENGLISH("en_US"),
    CHINESE_TRADITIONAL("zh_TW"),
    CHINESE_SIMPLIFIED("zh_CN"),
    HINDI("hi_IN"),
    SPANISH_SPAIN("es_ES"),
    SPANISH_MEXICO("es_MX"),
    FRENCH("fr_FR"),
    ARABIC("ar_SA"),
    RUSSIAN("ru_RU"),
    PORTUGUESE_PORTUGAL("pt_PT"),
    PORTUGUESE_BRAZIL("pt_BR"),
    GERMAN("de_DE"),
    JAPANESE("ja_JP"),
    TURKISH("tr_TR"),
    KOREAN("ko_KR"),
    VIETNAMESE("vi_VN"),
    ITALIAN("it_IT"),
    THAI("th_TH"),

    //rest listed alphabetically
    BULGARIAN("bg_BG"),
    CROATIAN("hr_HR"),
    CZECH("cs_CZ"),
    DANISH("da_DK"),
    DUTCH("nl_NL"),
    ESTONIAN("et_EE"),
    FINNISH("fi_FI"),
    GREEK("el_GR"),
    HEBREW("he_IL"),
    HUNGARIAN("hu_HU"),
    INDONESIAN("id_ID"),
    LITHUANIAN("lt_LT"),
    MALAY("ms_MY"),
    NORWEGIAN("no_NO"),
    PERSIAN("fa_IR"),
    POLISH("pl_PL"),
    ROMANIAN("ro_RO"),
    SERBIAN_LATIN("sr_CS"),
    SLOVENIAN("sl_SI"),
    SWEDISH("sv_SE"),
    UKRAINIAN("uk_UA"),
    PIRATE_ENGLISH("en_PT"),
    LOLCAT("lol_US"),
    BISCUITISH("bc_BC"),
    OWO("ow_Wo");

    private final ResourceLocation identifier;
    private final String path;

    Language(String path) {
        this.path = path;
        this.identifier = SkyblockAddons.resourceLocation("flags/" + path.toLowerCase(Locale.ENGLISH) + ".png");
    }

    /**
     * Find the corresponding {@link Language} to a given key string like {@code en_US}.
     * Case-insensitive.
     *
     * @param languageKey The lanugage key to look for.
     * @return The language if one was found, or null.
     */
    public static Language getFromPath(String languageKey) {
        for (Language language : values()) {
            String path = language.path;
            if (path != null && path.equalsIgnoreCase(languageKey)) {
                return language;
            }
        }
        return null;
    }

}