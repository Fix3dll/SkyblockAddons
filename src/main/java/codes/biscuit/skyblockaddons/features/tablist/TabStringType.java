package codes.biscuit.skyblockaddons.features.tablist;

import codes.biscuit.skyblockaddons.utils.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TabStringType {

    TITLE,
    SUB_TITLE,
    TEXT,
    PLAYER;

    private static final Pattern USERNAME_TAB_PATTERN = Pattern.compile("^\\[(?<sblevel>\\d+)] (?:\\[\\w+] )?(?<username>\\w+)");//(?: (?<subfix>[♲Ⓑ⚒ቾ]|\\[✌]))?(?: (?<faction>[⚒ቾ]))?(?: \\((?<dungeonClass>\\w+) ?(?<classLvl>\\w+)?\\))?$");
    public static TabStringType fromLine(String line) {
        String strippedLine = TextUtils.stripColor(line);

        if (strippedLine.startsWith(" ")) {
            return TEXT;
        }

        if (USERNAME_TAB_PATTERN.matcher(strippedLine).find()) {
            return PLAYER;
        } else {
            return SUB_TITLE;
        }
    }

    public static String usernameFromLine(String input) {
        Matcher usernameMatcher = USERNAME_TAB_PATTERN.matcher(TextUtils.stripColor(input));
        if (usernameMatcher.find())
            return usernameMatcher.group("username");
        else
            return input;
    }
}