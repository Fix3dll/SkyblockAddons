package com.fix3dll.skyblockaddons.features.tablist;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.EssenceType;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.Regex;
import com.fix3dll.skyblockaddons.core.SkillType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.spooky.SpookyEventManager;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class TabListParser {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static final String HYPIXEL_ADVERTISEMENT_CONTAINS = "HYPIXEL.NET";

    /** left is vanilla, right is parsed */
    @Getter private static RenderColumns renderColumns;
    @Getter private static String parsedRainTime;

    public static void parse() {
        if (!main.getUtils().isOnSkyblock() || isRelatedFeaturesDisabled()) {
            renderColumns = null;
            return;
        }

        if (MC.player == null || !MC.player.connection.isAcceptingMessages()) {
            renderColumns = null;
            return;
        }

        List<PlayerInfo> fullList = MC.gui.hud.getTabList().getPlayerInfos();
        if (fullList.size() < 80) {
            renderColumns = null;
            return;
        }

        // Parse into columns, combining any duplicate columns
        List<ParsedTabColumn> columns = parseColumns(fullList);
        ParsedTabColumn footerAsColumn = parseFooterAsColumn();

        if (footerAsColumn != null) {
            columns.add(footerAsColumn);
        }

        // Parse every column into sections
        parseSections(columns);

        // Combine columns into how they will be rendered
        renderColumns = new RenderColumns(fullList, new LinkedList<>());
        RenderColumn renderColumn = new RenderColumn();
        renderColumns.combinedRenderColumns.add(renderColumn);
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
        PlayerTabOverlay tabList = MC.gui.hud.getTabList();

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
                column.addLine(columnEntry, legacyFormatted);
            }
        }

        return columns;
    }

    public static ParsedTabColumn parseFooterAsColumn() {
        PlayerTabOverlay tabList = MC.gui.hud.getTabList();

        if (tabList.footer == null) {
            return null;
        }

        ParsedTabColumn column = new ParsedTabColumn("§2§lOthers");

        String legacyFormattedFooter = TextUtils.getFormattedText(tabList.footer);
        String footer = legacyFormattedFooter.replace("§s", "").replace("§S", "");
        String strippedFooter = TextUtils.stripColor(footer);

        // Make active effects/booster cookie status compact...
        Matcher m = Regex.GOD_POTION_PATTERN.matcher(strippedFooter);
        if (m.find()) {
            footer = Regex.ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: \n§cGod Potion§r: " + m.group("timer"));
        } else {
            if ((m = Regex.EFFECT_COUNT_PATTERN.matcher(strippedFooter)).find())
                footer = Regex.ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e" + m.group("effectCount"));
            else
                footer = Regex.ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e0");
        }

        if ((m = Regex.CANDY_FORMATTED_PATTERN.matcher(footer)).find()) {
            SpookyEventManager.update(
                    Integer.parseInt(m.group("green").replace(",", "")),
                    Integer.parseInt(m.group("purple").replace(",", "")),
                    Integer.parseInt(m.group("points").replace(",", ""))
            );
            footer = m.replaceAll("§7Your Candy: (§6" + m.group("points") + " §7pts.)"
                    + "\n §a" + m.group("green") + " Green"
                    + "\n §5" + m.group("purple") + " Purple");
        } else {
            SpookyEventManager.reset();
        }

        if ((m = Regex.COOKIE_BUFF_PATTERN.matcher(footer)).find() && m.group().contains("Not active!"))
            footer = m.replaceAll("Cookie Buff \n§r§7Not Active");

        if (main.getUtils().getJerryWave() != -1 && (m = Regex.JERRY_POWER_UPS_PATTERN.matcher(footer)).find()
                && m.group().contains("No Power Ups"))
            footer = m.replaceAll("Active Power Ups \n§r§7No Power Ups");

        footer = footer.replace("No Buffs active. Find them by exploring the Dungeon!", "No Buffs");

        for (String line : new ArrayList<>(Arrays.asList(footer.split("\n")))) {
            // Lets not add the advertisements to the columns
            if (line.contains(HYPIXEL_ADVERTISEMENT_CONTAINS)) continue;

            // Split every upgrade into 2 lines so it's not too long...
            if ((m = Regex.UPGRADES_PATTERN.matcher(TextUtils.stripResets(line))).matches()) {
                // Adds a space in front of any text that is not a sub-title
                String firstPart = TextUtils.trimWhitespaceAndResets(m.group("firstPart"));
                if (!firstPart.contains("§l")) {
                    firstPart = " " + firstPart;
                }
                column.addLine(80 + column.size(), firstPart);

                line = m.group("secondPart");
            }
            // Adds a space in front of any text that is not a sub-title
            line = TextUtils.trimWhitespaceAndResets(line);
            if (!line.contains("§l")) {
                line = " " + line;
            }

            column.addLine(80 + column.size(), line);
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
            for (Int2ObjectMap.Entry<String> line : column.getLines().int2ObjectEntrySet()) {
                // Empty lines reset the current section
                if (TextUtils.trimWhitespaceAndResets(line.getValue()).isEmpty()) {
                    foundSkillSection = false;
                    foundEssenceSection = false;
                    currentSection = null;
                    continue;
                }

                String stripped = TextUtils.stripColor(line.getValue()).trim();
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
                        && (m = Regex.RAIN_TIME_PATTERN.matcher(stripped)).matches()) {
                    parsedRainTime = m.group("time");
                }

                if (!foundSkillSection && !foundSkill) {
                    // The Catacombs still have old tab list instead of new Widgets
                    if (LocationUtils.isOn(Island.DUNGEON)
                            && (m = Regex.OLD_SKILL_LEVEL_PATTERN.matcher(stripped)).matches()) {
                        SkillType skillType = SkillType.getFromString(m.group("skill"));
                        int level = Integer.parseInt(m.group("level"));
                        main.getSkillXpManager().setSkillLevel(skillType, level);
                        foundSkill = true;
                    } else if (stripped.startsWith("Skills:")){
                        foundSkillSection = true;
                    }
                } else if (foundSkillSection && (m = Regex.SKILL_LEVEL_PATTERN.matcher(stripped)).matches()) {
                    SkillType skillType = SkillType.getFromString(m.group("skill"));
                    int level = Integer.parseInt(m.group("level"));
                    main.getSkillXpManager().setSkillLevel(skillType, level);
                }

                if (currentSection == null) {
                    column.addSection(currentSection = new ParsedTabSection(column));
                }

                currentSection.addLine(line.getIntKey(), line.getValue());
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
                        renderColumns.combinedRenderColumns.add(initialColumn = new RenderColumn());
                        currentCount = 1;
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT, -1));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE, -1));
                        currentCount++;
                    }

                    // Add lines 1 by 1, checking whether the count goes over the maximum.
                    // If it does go over the maximum add a new column
                    for (Int2ObjectMap.Entry<String> line : section.getLines().int2ObjectEntrySet()) {
                        if (currentCount >= TabListRenderer.MAX_LINES) {
                            renderColumns.combinedRenderColumns.add(initialColumn = new RenderColumn());
                            currentCount = 1;
                        }

                        String lineValue = line.getValue();
                        initialColumn.addLine(new TabLine(lineValue, TabStringType.fromLine(lineValue), line.getIntKey()));
                        currentCount++;
                    }
                } else {
                    // This section will cause this column to go over the max, so let's
                    // move on to the next column
                    if (currentCount + sectionSize > TabListRenderer.MAX_LINES) {
                        renderColumns.combinedRenderColumns.add(initialColumn = new RenderColumn());
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT, -1));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE, -1));
                    }

                    // And then add all the lines
                    for (Int2ObjectMap.Entry<String> line : section.getLines().int2ObjectEntrySet()) {
                        String lineValue = line.getValue();
                        initialColumn.addLine(new TabLine(lineValue, TabStringType.fromLine(lineValue), line.getIntKey()));
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

    /**
     * @param vanillaPlayerInfos vanilla tab list
     * @param combinedRenderColumns processed tab list
     */
    public record RenderColumns(List<PlayerInfo> vanillaPlayerInfos, List<RenderColumn> combinedRenderColumns) {
    }

}