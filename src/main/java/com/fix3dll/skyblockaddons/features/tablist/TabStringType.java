package com.fix3dll.skyblockaddons.features.tablist;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonClass;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonPlayer;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.RomanNumeralParser;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TabStringType {
    TITLE,
    SUB_TITLE,
    TEXT,
    PLAYER;

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final Pattern USERNAME_TAB_PATTERN = Pattern.compile("^\\[(?<sblevel>\\d+)] (?:\\[\\w+] )?(?<username>\\w+)(?:\\s*.\\s*\\((?<class>\\w+) (?<classLevel>\\w+)\\))?");

    public static TabStringType fromLine(String line) {
        String strippedLine = TextUtils.stripColor(line);

        if (strippedLine.startsWith(" ")) {
            return TEXT;
        }

        Matcher matcher = USERNAME_TAB_PATTERN.matcher(strippedLine);
        if (matcher.find()) {
            if (LocationUtils.isOn(Island.DUNGEON)) updateDungeonTeammateClass(matcher);
            return PLAYER;
        } else {
            return SUB_TITLE;
        }
    }

    public static String usernameFromLine(String input) {
        Matcher usernameMatcher = USERNAME_TAB_PATTERN.matcher(TextUtils.stripColor(input));
        if (usernameMatcher.find()) {
            return usernameMatcher.group("username");
        } else {
            return input;
        }
    }

    private static void updateDungeonTeammateClass(Matcher matcher) {
        if (matcher.groupCount() < 4) return;
        String tabUsername = matcher.group("username");
        String levelString = matcher.group("classLevel");
        if (StringUtil.isNullOrEmpty(tabUsername) || StringUtil.isNullOrEmpty(levelString)) {
            return;
        }
        int classLevel;
        try {
            if (levelString.equals("0")) return;
            classLevel = RomanNumeralParser.parseNumeral(levelString);
        } catch (Exception ex) {
            LOGGER.error("Error parsing " + tabUsername + "'s class level:", ex);
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        Pair<DungeonClass, Integer> thePlayerClass = main.getDungeonManager().getThePlayerClass();
        LocalPlayer player = Minecraft.getInstance().player;
        if (thePlayerClass == null && player != null && tabUsername.equals(player.getGameProfile().getName())) {
            String dungeonClassString = matcher.group("class");
            if (!StringUtil.isNullOrEmpty(dungeonClassString)) {
                DungeonClass dungeonClass = DungeonClass.fromFirstLetter(dungeonClassString.charAt(0));
                if (dungeonClass != null) {
                    main.getDungeonManager().setThePlayerClass(new Pair<>(dungeonClass, classLevel));
                }
            }
        } else {
            for (Map.Entry<String, DungeonPlayer> entry : main.getDungeonManager().getTeammates().entrySet()) {
                String teammateName = entry.getKey();
                DungeonPlayer teammateData = entry.getValue();

                if (tabUsername.equals(teammateName)) {
                    teammateData.setClassLevel(classLevel);
                }
            }
        }
    }
}