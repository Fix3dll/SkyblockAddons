package codes.biscuit.skyblockaddons.features.tablist;

import codes.biscuit.skyblockaddons.utils.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TabStringType {

    TITLE,
    SUB_TITLE,
    TEXT,
    PLAYER;

    private static final Pattern USERNAME_TAB_PATTERN = Pattern.compile("^§r§8\\[§r§.(?<sblevel>\\d+)§r§8\\] §r§.(?<username>\\w+)(?: §r§.(?<subfix>[♲Ⓑ⚒ቾ]|\\[✌\\]))?(?: §r§.(?<faction>[⚒ቾ]))?§r$");
    public static TabStringType fromLine(String line) {
        String strippedLine = TextUtils.stripColor(line);

        if (strippedLine.startsWith(" ")) {
            return TEXT;
        }

        if (!line.contains("§l") && USERNAME_TAB_PATTERN.matcher(line).matches()) {
            return PLAYER;
        } else {
            return SUB_TITLE;
        }
    }

    public static String usernameFromLine(String input) {
        Matcher usernameMatcher = USERNAME_TAB_PATTERN.matcher(input);
        if (usernameMatcher.matches())
            return usernameMatcher.group("username");
        else
            return input;
    }
}
