package com.fix3dll.skyblockaddons.utils;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Roman numerals
 * TODO improvements
 */
public class RomanNumeralParser {

    /**
     * Pattern that validates a string as a correct Roman numeral
     */
    private static final Pattern NUMERAL_VALIDATION_PATTERN = Pattern.compile("^(?=[MDCLXVI])M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
    /**
     * Pattern that finds words that begin with a Roman numeral
     */
    private static final Pattern NUMERAL_FINDING_PATTERN = Pattern.compile(" (?=[MDCLXVI])(?<roman>M*(?:C[MD]|D?C{0,3})(?:X[CL]|L?X{0,3})(?:I[XV]|V?I{0,3}))(?<after>(?: ✖|.)?)");

    /**
     * Map that contains mappings for decimal-to-roman conversion
     */
    private static final TreeMap<Integer, String> INT_ROMAN_MAP = new TreeMap<>();
    static {
        INT_ROMAN_MAP.put(1000, "M");
        INT_ROMAN_MAP.put(900, "CM");
        INT_ROMAN_MAP.put(500, "D");
        INT_ROMAN_MAP.put(400, "CD");
        INT_ROMAN_MAP.put(100, "C");
        INT_ROMAN_MAP.put(90, "XC");
        INT_ROMAN_MAP.put(50, "L");
        INT_ROMAN_MAP.put(40, "XL");
        INT_ROMAN_MAP.put(10, "X");
        INT_ROMAN_MAP.put(9, "IX");
        INT_ROMAN_MAP.put(5, "V");
        INT_ROMAN_MAP.put(4, "IV");
        INT_ROMAN_MAP.put(1, "I");
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
            try {
                return Numeral.valueOf(Character.toString(c));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Expected valid Roman numeral, received '" + c + "'.");
            }
        }
    }


    public static String integerToRoman(int number) {
        int l = INT_ROMAN_MAP.floorKey(number);
        if (number == l) {
            return INT_ROMAN_MAP.get(number);
        }
        return INT_ROMAN_MAP.get(l) + integerToRoman(number - l);
    }

    /**
     * Replaces all occurrences of Roman numerals in an input string with their integer values.
     * For example: VI -> 6, X -> 10, etc
     *
     * @param input Input string to replace numerals in
     * @return The input string with all numerals replaced by integers
     */
    public static String replaceNumeralsWithIntegers(String input) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = NUMERAL_FINDING_PATTERN.matcher(input);

        // The matcher finds all words after a space that begin with a Roman numeral.
        while (matcher.find()) {
            int parsedInteger;
            String roman = matcher.group("roman");
            String after = matcher.group("after");
            Matcher wordPartMatcher = Pattern.compile("^[\\w-']").matcher(after);

            // Ignore this match if it is a capital letter that is part of a word or if the first capture group matches an empty String.
            if (wordPartMatcher.matches() || roman.isEmpty()) {
                continue;
            }

            parsedInteger = parseNumeral(roman);

            // Don't replace the word "I" and don't miss attributes
            if (parsedInteger != 1 || after.equals("§") || after.isEmpty() || after.equals(" ✖")) {
                matcher.appendReplacement(result, " " + parsedInteger + "$2");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Tests whether an input string is a valid Roman numeral.
     * To be valid the numerals must be either {@code I, V, X, L, C, D, M} and in upper case
     * and in correct format (meaning {@code IIII} is invalid as it should be {@code IV})
     *
     * @param romanNumeral String to test
     * @return Whether that string represents a valid Roman numeral
     */
    public static boolean isNumeralValid(String romanNumeral) {
        return NUMERAL_VALIDATION_PATTERN.matcher(romanNumeral).matches();
    }

    /**
     * Parses a valid Roman numeral string to its integer value.
     * Use {@link #isNumeralValid(String)} to check.
     *
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
        char[] charArray = numeralString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            Numeral numeral = Numeral.getFromChar(c);
            if (i + 1 < charArray.length) {
                // check next numeral to correctly evaluate IV, IX and so forth
                Numeral nextNumeral = Numeral.getFromChar(charArray[i + 1]);
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

}