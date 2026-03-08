package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translations {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%[A-Za-z-]+%");
    private static final Pattern PATH_SPLIT_PATTERN = Pattern.compile("\\.");

    @Getter @Setter private static JsonObject languageJson = new JsonObject();
    @Setter private static JsonObject defaultLangJson = null;

    public static String getMessage(String path, Object... variables) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main == null || main.getConfigValuesManager() == null || languageJson == null) {
            return path;
        }

        String text = getString(languageJson, path);

        if (text.isEmpty()) {
            text = getString(defaultLangJson, path);
        }

        // If after fallback it's still empty, return the path.
        if (text.isEmpty()) {
            return path;
        }

        if (variables != null && variables.length > 0) {
            Matcher matcher = VARIABLE_PATTERN.matcher(text);
            StringBuilder sb = null;
            int i = 0;
            while (matcher.find() && i < variables.length) {
                if (sb == null) sb = new StringBuilder(text.length() + 16);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(variables[i++])));
            }
            if (sb != null) {
                matcher.appendTail(sb);
                text = sb.toString();
            }
        }

        // Handle RTL text...
        Language currentLanguage = (Language) Feature.LANGUAGE.getValue();
        if ((currentLanguage == Language.HEBREW || currentLanguage == Language.ARABIC)
                && !MC.font.isBidirectional()) {
            text = MC.font.bidirectionalShaping(text);
        }

        return text;
    }

    private static String getString(JsonObject langJson, String path) {
        if (langJson == null || path == null || path.isEmpty()) {
            return "";
        }

        String[] pathSplit = PATH_SPLIT_PATTERN.split(path);
        for (String pathPart : pathSplit) {
            if (pathPart.isEmpty()) continue;

            JsonElement jsonElement = langJson.get(pathPart);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                return "";
            } else if (jsonElement.isJsonObject()) {
                langJson = jsonElement.getAsJsonObject();
            } else {
                return jsonElement.getAsString();
            }
        }
        return "";
    }

}