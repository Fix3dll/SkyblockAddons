package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.core.Regex;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

/**
 * Utility class for working with Roman numerals
 */
public class RomanNumeralParser {

    /**
     * Map that contains mappings for decimal-to-roman conversion
     */
    private static final TreeMap<Integer, String> INT_ROMAN_MAP = new TreeMap<>(Map.ofEntries(
            Map.entry(1000, "M"),
            Map.entry(900,  "CM"),
            Map.entry(500,  "D"),
            Map.entry(400,  "CD"),
            Map.entry(100,  "C"),
            Map.entry(90,   "XC"),
            Map.entry(50,   "L"),
            Map.entry(40,   "XL"),
            Map.entry(10,   "X"),
            Map.entry(9,    "IX"),
            Map.entry(5,    "V"),
            Map.entry(4,    "IV"),
            Map.entry(1,    "I")
    ));

    /**
     * Converts an integer to its Roman numeral representation iteratively.
     */
    public static String integerToRoman(int number) {
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            var entry = INT_ROMAN_MAP.floorEntry(number);
            result.append(entry.getValue());
            number -= entry.getKey();
        }
        return result.toString();
    }

    /**
     * Replaces all occurrences of Roman numerals in an input component with their integer values,
     * preserving all styles, click events, and hover events in the component tree.
     * For example: VI -> 6, X -> 10, etc.
     * @param inputComponent the component to replace numerals in
     * @return a new component with all numerals replaced by integers, or the original component
     *         if no replacements were made
     */
    public static Component replaceNumeralsWithIntegers(Component inputComponent) {
        String inputString = inputComponent.getString();
        Matcher matcher = Regex.NUMERAL_FINDING_PATTERN.matcher(inputString);
        Component result = inputComponent;
        boolean modified = false;

        // The matcher finds all words after a space that begin with a Roman numeral.
        while (matcher.find()) {
            String roman = matcher.group("roman");
            String after = matcher.group("after");

            // Ignore this match if it is a capital letter that is part of a word or if the first capture group matches an empty String.
            if (Regex.WORD_PART_PATTERN.matcher(after).matches() || roman.isEmpty()) {
                continue;
            }

            int parsedInteger = parseNumeral(roman);

            // Don't replace the word "I" and don't miss attributes
            if (parsedInteger != 1 || after.equals("§") || after.isEmpty() || after.equals(" ✖")) {
                result = TextUtils.replaceComponent(result, " " + roman, " " + parsedInteger);
                modified = true;
            }
        }

        return modified ? result : inputComponent;
    }

    /**
     * Tests whether an input string is a valid Roman numeral.
     * To be valid the numerals must be either {@code I, V, X, L, C, D, M} and in upper case
     * and in correct format (meaning {@code IIII} is invalid as it should be {@code IV})
     * @param romanNumeral String to test
     * @return Whether that string represents a valid Roman numeral
     */
    public static boolean isNumeralValid(String romanNumeral) {
        return Regex.NUMERAL_VALIDATION_PATTERN.matcher(romanNumeral).matches();
    }

    /**
     * Parses a valid Roman numeral string to its integer value.
     * Use {@link #isNumeralValid(String)} to check.
     * @param numeralString Numeral to parse
     * @return Parsed value
     * @throws IllegalArgumentException If the input is malformed
     */
    public static int parseNumeral(String numeralString) {
        // Make sure this is a valid Roman numeral before trying to parse it.
        if (!isNumeralValid(numeralString)) {
            throw new IllegalArgumentException("\"" + numeralString + "\" is not a valid Roman numeral.");
        }

        int value = 0; // parsed value
        int length = numeralString.length();
        for (int i = 0; i < length; i++) {
            Numeral numeral = Numeral.getFromChar(numeralString.charAt(i));
            if (i + 1 < length) {
                // check next numeral to correctly evaluate IV, IX and so forth
                Numeral nextNumeral = Numeral.getFromChar(numeralString.charAt(i + 1));
                int diff = nextNumeral.value - numeral.value;
                if (diff > 0) {
                    // if the next numeral is of higher value, it means their difference should be added instead
                    value += diff;
                    i++; // skip next char
                    continue;
                }
            }
            value += numeral.value;
        }
        return value;
    }

    private enum Numeral {
        I(1),
        V(5),
        X(10),
        L(50),
        C(100),
        D(500),
        M(1000);

        private final int value;

        Numeral(int value) {
            this.value = value;
        }

        private static Numeral getFromChar(char c) {
            return switch (c) {
                case 'I' -> I;
                case 'V' -> V;
                case 'X' -> X;
                case 'L' -> L;
                case 'C' -> C;
                case 'D' -> D;
                case 'M' -> M;
                default -> throw new IllegalArgumentException("Expected valid Roman numeral, received '" + c + "'.");
            };
        }
    }

}