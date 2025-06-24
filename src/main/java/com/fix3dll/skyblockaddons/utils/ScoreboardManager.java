package com.fix3dll.skyblockaddons.utils;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class ScoreboardManager {

    public static final Pattern SIDEBAR_EMOJI_PATTERN = Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+");

    @Getter private static String scoreboardTitle;
    @Getter private static String strippedScoreboardTitle;

    @Getter private static List<String> scoreboardLines;
    @Getter private static List<String> strippedScoreboardLines;

    @Getter private static long lastFoundScoreboard = -1;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isLocalServer()) {
            clear();
            return;
        }

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebarObjective == null) {
            clear();
            return;
        }

        lastFoundScoreboard = System.currentTimeMillis();

        // Update titles
        scoreboardTitle = TextUtils.getFormattedText(sidebarObjective.getDisplayName(), true);
        strippedScoreboardTitle = sidebarObjective.getDisplayName().getString();

        // Update score lines
        Collection<String> lines = scoreboard.listPlayerScores(sidebarObjective).stream()
                .filter(scoreboardEntry -> !scoreboardEntry.isHidden())
                .sorted(Gui.SCORE_DISPLAY_ORDER)
                .limit(15)
                .map(scoreboardEntry -> {
                    String owner = scoreboardEntry.owner();
                    Component name = scoreboardEntry.ownerName();

                    PlayerTeam team = scoreboard.getPlayersTeam(owner);
                    Component decoratedName = PlayerTeam.formatNameForTeam(team, name);

                    // return fixed name
                    return TextUtils.getFormattedText(decoratedName, true).replace(owner, "").trim();
                })
                .toList();

        scoreboardLines = new ArrayList<>();
        strippedScoreboardLines = new ArrayList<>();

        for (String line : lines) {
            // TODO cleanup
            String cleansedScoreboardLine = SIDEBAR_EMOJI_PATTERN.matcher(line).replaceAll("");
            String strippedCleansedScoreboardLine = TextUtils.stripColor(cleansedScoreboardLine);

            scoreboardLines.add(cleansedScoreboardLine);
            strippedScoreboardLines.add(strippedCleansedScoreboardLine);
        }

    }

    private static void clear() {
        scoreboardTitle = strippedScoreboardTitle = null;
        scoreboardLines = strippedScoreboardLines = null;
    }

    public static boolean hasScoreboard() {
        return scoreboardTitle != null;
    }

    public static int getNumberOfLines() {
        return scoreboardLines.size();
    }
}
