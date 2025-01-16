package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translations {

    @Getter @Setter private static JsonObject languageJson = new JsonObject();
    @Setter private static JsonObject defaultLangJson = null;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%[A-Za-z-]+%");

    public static String getMessage(String path, Object... variables) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        String text;

        // Get the string.
        JsonObject langJson = null;
        if (main.getConfigValuesManager() != null)
            langJson = languageJson;

        if (langJson != null) {
            text = getString(langJson, path);
        } else {
            return path;
        }

        // FALLBACK
        if (text.isEmpty()) text = getString(defaultLangJson, path);
        // If there is no translation on fallback, return path
        if (text.isEmpty()) return path;

        // Iterate through the string and replace any variables.
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        Deque<Object> variablesDeque = new ArrayDeque<>(Arrays.asList(variables));

        while (matcher.find() && !variablesDeque.isEmpty()) {
            // Replace a variable and re-make the matcher.
            text = matcher.replaceFirst(Matcher.quoteReplacement(variablesDeque.pollFirst().toString()));
            matcher = VARIABLE_PATTERN.matcher(text);
        }

        // Handle RTL text...
        Language currentLanguage = (Language) Feature.LANGUAGE.getValue();
        if ((currentLanguage == Language.HEBREW || currentLanguage == Language.ARABIC)
                && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
            text = bidiReorder(text);
        }

        return text;
    }

    private static String getString(JsonObject langJson, String path) {
        if (langJson == null) return "";
        String[] pathSplit = path.split(Pattern.quote("."));
        for (String pathPart : pathSplit) {
            if (!pathPart.isEmpty()) {
                JsonElement jsonElement = langJson.get(pathPart);

                if (jsonElement == null) {
                    return "";
                } else if (jsonElement.isJsonObject()) {
                    langJson = langJson.getAsJsonObject(pathPart);
                } else {
                    return langJson.get(path.substring(path.lastIndexOf(pathPart))).getAsString();
                }
            }
        }
        return "";
    }

    private static String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(ArabicShaping.LETTERS_SHAPE)).shape(text), Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException ex) {
            return text;
        }
    }
}