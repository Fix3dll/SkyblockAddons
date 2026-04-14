package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Regex;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.gson.PatternAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Tests for {@link TextUtils}
 */
public class TextUtilsTests {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Pattern.class, new PatternAdapter())
            .create();

    @BeforeAll
    static void setup() {
        // Regex Patterns Data
        try (InputStream inputStream = DataUtils.class.getResourceAsStream("/regex.json");
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)){
            Map<String, Pattern> data = GSON.fromJson(inputStreamReader, new TypeToken<Map<String, Pattern>>() {}.getType());
            for (Map.Entry<String, Pattern> entry : data.entrySet()) {
                String key = entry.getKey();
                Pattern pattern = entry.getValue();
                try {
                    Regex.valueOf(key).setPattern(pattern);
                } catch (IllegalArgumentException e) {
                    try {
                        InventoryType.valueOf(key).setInventoryPattern(pattern);
                    } catch (IllegalArgumentException e2) {
                        System.err.printf("Patterns data contains unrecognized key '%s', skipping.\n", key);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @DisplayName("Action Bar Magnitude Conversion Tests")
    @ParameterizedTest()
    @CsvFileSource(resources = "/convert-magnitudes.csv", numLinesToSkip = 1)
    void testActionBarMagnitudeConversions(String inputString, String expectedOutput) {
        try {
            Assertions.assertEquals(expectedOutput, TextUtils.convertMagnitudes(inputString));
        } catch (ParseException e) {
            Assertions.fail("Failed to parse number at offset " + e.getErrorOffset() + " in string \"" + e.getMessage() + "\".");
        }
    }
}
