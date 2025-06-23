package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.utils.TextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.text.ParseException;

/**
 * Tests for {@link TextUtils}
 */
public class TextUtilsTests {

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
