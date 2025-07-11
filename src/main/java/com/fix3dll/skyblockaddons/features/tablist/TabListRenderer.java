package com.fix3dll.skyblockaddons.features.tablist;

import com.fix3dll.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.LightTexture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabListRenderer {

    private static final Font FONT = Minecraft.getInstance().font;

    public static final int MAX_LINES = 22;
    private static final int LINE_HEIGHT = 8 + 1;
    private static final int PADDING = 3;
    private static final int COLUMN_SPACING = 6;

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();

        List<RenderColumn> columns = TabListParser.getRenderColumns();
        if (columns == null) {
            return;
        }

        // Calculate maximums...
        int maxLines = 0;
        for (RenderColumn column : columns) {
            maxLines = Math.max(maxLines, column.getLines().size());
        }
        int totalWidth = 0;
        for (RenderColumn renderColumn : columns) {
            totalWidth += renderColumn.getMaxWidth() + COLUMN_SPACING;
        }
        totalWidth -= COLUMN_SPACING;
        int totalHeight = maxLines * LINE_HEIGHT;

        // Filter header and footer to only show hypixel advertisements...
        PlayerTabOverlay tabList = mc.gui.getTabList();
        List<String> header = null;
        if (tabList.header != null) {
            String legacyFormattedHeader = TextUtils.getFormattedText(tabList.header);
            header = new ArrayList<>(Arrays.asList(legacyFormattedHeader.split("\n")));
            header.removeIf((line) -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS));

            totalHeight += header.size() * LINE_HEIGHT + PADDING;
        }
        List<String> footer = null;
        if (tabList.footer != null) {
            String legacyFormatteFooter = TextUtils.getFormattedText(tabList.footer);
            footer = new ArrayList<>(Arrays.asList(legacyFormatteFooter.split("\n")));
            footer.removeIf((line) -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS));

            totalHeight += footer.size() * LINE_HEIGHT + PADDING;
        }

        // Starting x & y, using the player's GUI scale
        int screenWidth = mc.getWindow().getGuiScaledWidth() / 2;
        int x = screenWidth - totalWidth / 2;
        int y = 10;

        // Large background
        graphics.fill(x - COLUMN_SPACING, y - PADDING, screenWidth + totalWidth/2 + COLUMN_SPACING, 10 + totalHeight + PADDING, 0x80000000);

        // Draw header
        int headerY = y;
        if (header != null) {
            final int fTW = totalWidth;
            for (String line : header) {
                final int fHY = headerY;
                graphics.drawSpecial(source -> FONT.drawInBatch(line, x + fTW / 2F - mc.font.width(line) / 2F, fHY, -1, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT));
                headerY += 8 + 1;
            }
            headerY += PADDING;
        }

        // Draw the middle lines
        int middleX = x;
        for (RenderColumn renderColumn : columns) {
            int middleY = headerY;

            // Column background
            graphics.fill(
                    middleX - PADDING + 1,
                    middleY - PADDING + 1,
                    middleX + renderColumn.getMaxWidth() + PADDING - 2,
                    middleY + renderColumn.getLines().size() * LINE_HEIGHT + PADDING - 2,
                    0x20AAAAAA
            );

            for (TabLine tabLine : renderColumn.getLines()) {
                int savedX = middleX;

                if (tabLine.type() == TabStringType.PLAYER && mc.getConnection() != null) {
                    PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(TabStringType.usernameFromLine(tabLine.text()));
                    if (playerInfo != null) {
                        PlayerFaceRenderer.draw(graphics, playerInfo.getSkin(), middleX, middleY, 8);
                    }
                    middleX += 8 + 2;
                }

                final int fX = middleX, fY = middleY;
                if (tabLine.type() == TabStringType.TITLE) {
                    graphics.drawSpecial(source -> FONT.drawInBatch(tabLine.text(), (fX + renderColumn.getMaxWidth() / 2F - tabLine.getWidth() / 2F), fY , -1, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT));
                } else {
                    graphics.drawSpecial(source -> FONT.drawInBatch(tabLine.text(), fX, fY , -1, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT));
                }
                middleY += LINE_HEIGHT;
                middleX = savedX;
            }

            middleX += renderColumn.getMaxWidth() + COLUMN_SPACING;
        }

        // Draw the footer
        if (footer != null) {
            int footerY = y + totalHeight - footer.size() * LINE_HEIGHT;
            final int fTW = totalWidth;
            for (String line : footer) {
                final int fFY = footerY;
                graphics.drawSpecial(source -> FONT.drawInBatch(line, x + fTW / 2F - mc.font.width(line) / 2F, fFY , -1, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT));
                footerY += LINE_HEIGHT;
            }
        }
    }
}