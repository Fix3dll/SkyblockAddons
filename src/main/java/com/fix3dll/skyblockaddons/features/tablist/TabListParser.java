package com.fix3dll.skyblockaddons.features.tablist;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.EssenceType;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.SkillType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.spooky.SpookyEventManager;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabListParser {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static final String HYPIXEL_ADVERTISEMENT_CONTAINS = "HYPIXEL.NET";

    private static final Pattern GOD_POTION_PATTERN = Pattern.compile("You have a God Potion active! (?<timer>[\\w ]+)");
    private static final Pattern ACTIVE_EFFECTS_PATTERN = Pattern.compile("Active Effects(?:§.)*(?:\\n(?:§.)*§7.+)*");
    private static final Pattern EFFECT_COUNT_PATTERN = Pattern.compile("You have (?<effectCount>[0-9]+) active effect");
    private static final Pattern COOKIE_BUFF_PATTERN = Pattern.compile("Cookie Buff(?:§.)*(?:\\n(§.)*§7.+)*");
    private static final Pattern UPGRADES_PATTERN = Pattern.compile("(?<firstPart>§e[A-Za-z ]+)(?<secondPart> §f[\\w ]+)");
    private static final Pattern CANDY_PATTERN = Pattern.compile("Your Candy: §r§a(?<green>[0-9,]+) Green§r§7, §r§5(?<purple>[0-9,]+) Purple §r§7\\(§r§6(?<points>[0-9,]+) §r§7pts\\.\\)");
    private static final Pattern DUNGEON_BUFF_PATTERN = Pattern.compile("No Buffs active. Find them by exploring the Dungeon!");
    private static final Pattern RAIN_TIME_PATTERN = Pattern.compile("Rain: (?<time>[0-9dhms ]+)");
    private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile("(?<skill>[A-Za-z]+) (?<level>[0-9]+): (?:[0-9.,]+%|MAX)?");
    private static final Pattern OLD_SKILL_LEVEL_PATTERN = Pattern.compile("Skills: (?<skill>[A-Za-z]+) (?<level>[0-9]+).*");
    private static final Pattern JERRY_POWER_UPS_PATTERN = Pattern.compile("Active Power Ups(?:§.)*(?:\\n(§.)*§7.+)*");

    @Getter private static List<RenderColumn> renderColumns;
    @Getter private static String parsedRainTime;

    public static void parse() {
        Minecraft mc = Minecraft.getInstance();

        if (!main.getUtils().isOnSkyblock() || isRelatedFeaturesDisabled()) {
            renderColumns = null;
            return;
        }

        if (mc.player == null || !mc.player.connection.isAcceptingMessages()) {
            renderColumns = null;
            return;
        }

        ClientPacketListener connection = mc.player.connection;
        List<PlayerInfo> fullList = connection.getListedOnlinePlayers().stream()
                .sorted(Comparator.comparing(playerInfo -> playerInfo.getProfile().name()))
                .toList();
        if (fullList.size() < 80) {
            renderColumns = null;
            return;
        }
        fullList = fullList.subList(0, 80);


        // Parse into columns, combining any duplicate columns
        List<ParsedTabColumn> columns = parseColumns(fullList);
        ParsedTabColumn footerAsColumn = parseFooterAsColumn();

        if (footerAsColumn != null) {
            columns.add(footerAsColumn);
        }

        // Parse every column into sections
        parseSections(columns);

        // Combine columns into how they will be rendered
        renderColumns = new LinkedList<>();
        RenderColumn renderColumn = new RenderColumn();
        renderColumns.add(renderColumn);
        combineColumnsToRender(columns, renderColumn);
    }

    public static ParsedTabColumn getColumnFromName(List<ParsedTabColumn> columns, String name) {
        for (ParsedTabColumn parsedTabColumn : columns) {
            if (name.equals(parsedTabColumn.getTitle())) {
                return parsedTabColumn;
            }
        }

        return null;
    }

    private static List<ParsedTabColumn> parseColumns(List<PlayerInfo> fullList) {
        PlayerTabOverlay tabList = Minecraft.getInstance().gui.getTabList();

        List<ParsedTabColumn> columns = new LinkedList<>();
        for (int entry = 0; entry < fullList.size(); entry += 20) {
            String title = TextUtils.getFormattedText(tabList.getNameForDisplay(fullList.get(entry))).trim();
            ParsedTabColumn column = getColumnFromName(columns, title);
            if (column == null) {
                column = new ParsedTabColumn(title);
                columns.add(column);
            }

            for (int columnEntry = entry + 1; columnEntry < fullList.size() && columnEntry < entry + 20; columnEntry++) {
                String legacyFormatted = TextUtils.getFormattedText(tabList.getNameForDisplay(fullList.get(columnEntry)));
                column.addLine(legacyFormatted);
            }
        }

        return columns;
    }

    private static final Pattern TABLIST_S = Pattern.compile("(?i)§S");
    public static ParsedTabColumn parseFooterAsColumn() {
        PlayerTabOverlay tabList = Minecraft.getInstance().gui.getTabList();

        if (tabList.footer == null) {
            return null;
        }

        ParsedTabColumn column = new ParsedTabColumn("§2§lOthers");

        String legacyFormattedFooter = TextUtils.getFormattedText(tabList.footer);
        String footer = TABLIST_S.matcher(legacyFormattedFooter).replaceAll("");
        String strippedFooter = TextUtils.stripColor(footer);

        // Make active effects/booster cookie status compact...
        Matcher m = GOD_POTION_PATTERN.matcher(strippedFooter);
        if (m.find()) {
            footer = ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: \n§cGod Potion§r: " + m.group("timer"));
        } else {
            if ((m = EFFECT_COUNT_PATTERN.matcher(strippedFooter)).find())
                footer = ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e" + m.group("effectCount"));
            else
                footer = ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e0");
        }

        if ((m = CANDY_PATTERN.matcher(footer)).find()) {
            SpookyEventManager.update(
                    Integer.parseInt(m.group("green").replaceAll(",", "")),
                    Integer.parseInt(m.group("purple").replaceAll(",", "")),
                    Integer.parseInt(m.group("points").replaceAll(",", ""))
            );
            footer = m.replaceAll("§7Your Candy: (§6" + m.group("points") + " §7pts.)"
                    + "\n §a" + m.group("green") + " Green"
                    + "\n §5" + m.group("purple") + " Purple");
        } else {
            SpookyEventManager.reset();
        }

        if ((m = COOKIE_BUFF_PATTERN.matcher(footer)).find() && m.group().contains("Not active!"))
            footer = m.replaceAll("Cookie Buff \n§r§7Not Active");

        if (main.getUtils().getJerryWave() != -1 && (m = JERRY_POWER_UPS_PATTERN.matcher(footer)).find()
                && m.group().contains("No Power Ups"))
            footer = m.replaceAll("Active Power Ups \n§r§7No Power Ups");

        if ((m = DUNGEON_BUFF_PATTERN.matcher(footer)).find())
            footer = m.replaceAll("No Buffs");

        for (String line : new ArrayList<>(Arrays.asList(footer.split("\n")))) {
            // Lets not add the advertisements to the columns
            if (line.contains(HYPIXEL_ADVERTISEMENT_CONTAINS)) continue;

            // Split every upgrade into 2 lines so it's not too long...
            if ((m = UPGRADES_PATTERN.matcher(TextUtils.stripResets(line))).matches()) {
                // Adds a space in front of any text that is not a sub-title
                String firstPart = TextUtils.trimWhitespaceAndResets(m.group("firstPart"));
                if (!firstPart.contains("§l")) {
                    firstPart = " " + firstPart;
                }
                column.addLine(firstPart);

                line = m.group("secondPart");
            }
            // Adds a space in front of any text that is not a sub-title
            line = TextUtils.trimWhitespaceAndResets(line);
            if (!line.contains("§l")) {
                line = " " + line;
            }

            column.addLine(line);
        }

        return column;
    }

    public static void parseSections(List<ParsedTabColumn> columns) {
        parsedRainTime = null;
        boolean foundEssenceSection = false;
        boolean foundSkillSection = false;
        boolean foundSkill = false;
        for (ParsedTabColumn column : columns) {
            ParsedTabSection currentSection = null;
            for (String line : column.getLines()) {
                // Empty lines reset the current section
                if (TextUtils.trimWhitespaceAndResets(line).isEmpty()) {
                    foundSkillSection = false;
                    foundEssenceSection = false;
                    currentSection = null;
                    continue;
                }

                String stripped = TextUtils.stripColor(line).trim();
                Matcher m;

                if (!foundEssenceSection && Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)
                        && stripped.contains("Essence:")) {
                    foundEssenceSection = true;
                }

                if (foundEssenceSection) {
                    String num = stripped.substring(stripped.indexOf(" ") + 1);
                    for (EssenceType type : EssenceType.values()) {
                        if (stripped.contains(type.getNiceName())) {
                            main.getDungeonManager().setSalvagedEssences(type, num);
                            break;
                        }
                    }
                }

                if (parsedRainTime == null && Feature.BIRCH_PARK_RAINMAKER_TIMER.isEnabled()
                        && LocationUtils.isOn("Birch Park")
                        && (m = RAIN_TIME_PATTERN.matcher(stripped)).matches()) {
                    parsedRainTime = m.group("time");
                }

                if (!foundSkillSection && !foundSkill) {
                    // The Catacombs still have old tab list instead of new Widgets
                    if (LocationUtils.isOn(Island.DUNGEON)
                            && (m = OLD_SKILL_LEVEL_PATTERN.matcher(stripped)).matches()) {
                        SkillType skillType = SkillType.getFromString(m.group("skill"));
                        int level = Integer.parseInt(m.group("level"));
                        main.getSkillXpManager().setSkillLevel(skillType, level);
                        foundSkill = true;
                    } else if (stripped.startsWith("Skills:")){
                        foundSkillSection = true;
                    }
                } else if (foundSkillSection && (m = SKILL_LEVEL_PATTERN.matcher(stripped)).matches()) {
                    SkillType skillType = SkillType.getFromString(m.group("skill"));
                    int level = Integer.parseInt(m.group("level"));
                    main.getSkillXpManager().setSkillLevel(skillType, level);
                }

                if (currentSection == null) {
                    column.addSection(currentSection = new ParsedTabSection(column));
                }

                currentSection.addLine(line);
            }
        }
    }

    @SuppressWarnings("StringEquality")
    public static void combineColumnsToRender(List<ParsedTabColumn> columns, RenderColumn initialColumn) {
        String lastTitle = null;
        for (ParsedTabColumn column : columns) {
            for (ParsedTabSection section : column.getSections()) {
                int sectionSize = section.size();

                // Check if we need to add the column title before this section
                boolean needsTitle = false;
                if (lastTitle != section.getColumn().getTitle()) {
                    needsTitle = true;
                    sectionSize++;
                }

                int currentCount = initialColumn.size();

                // The section is larger than max lines, we need to overflow
                if (sectionSize >= TabListRenderer.MAX_LINES / 2) { // TODO Double check this?

                    // If we are already at the max, we must start a new
                    // column so the title isn't by itself
                    if (currentCount >= TabListRenderer.MAX_LINES) {
                        renderColumns.add(initialColumn = new RenderColumn());
                        currentCount = 1;
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE));
                        currentCount++;
                    }

                    // Add lines 1 by 1, checking whether the count goes over the maximum.
                    // If it does go over the maximum add a new column
                    for (String line : section.getLines()) {
                        if (currentCount >= TabListRenderer.MAX_LINES) {
                            renderColumns.add(initialColumn = new RenderColumn());
                            currentCount = 1;
                        }

                        initialColumn.addLine(new TabLine(line, TabStringType.fromLine(line)));
                        currentCount++;
                    }
                } else {
                    // This section will cause this column to go over the max, so let's
                    // move on to the next column
                    if (currentCount + sectionSize > TabListRenderer.MAX_LINES) {
                        renderColumns.add(initialColumn = new RenderColumn());
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE));
                    }

                    // And then add all the lines
                    for (String line : section.getLines()) {
                        initialColumn.addLine(new TabLine(line, TabStringType.fromLine(line)));
                    }
                }
            }
        }
    }

    /**
     * @return true If related features disabled
     */
    private static boolean isRelatedFeaturesDisabled() {
        return Feature.COMPACT_TAB_LIST.isDisabled()
                && Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isDisabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)
                && Feature.BIRCH_PARK_RAINMAKER_TIMER.isDisabled()
                && Feature.CANDY_POINTS_COUNTER.isDisabled()
                && (Feature.SKILL_DISPLAY.isDisabled()
                || Feature.SKILL_DISPLAY.isEnabled(FeatureSetting.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP));
    }
}