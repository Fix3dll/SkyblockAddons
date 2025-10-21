package com.fix3dll.skyblockaddons.features.tablist;

import com.fix3dll.skyblockaddons.core.render.state.SbaTextRenderState;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

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
            for (String line : header) {
                FormattedCharSequence lineFcs = Language.getInstance().getVisualOrder(FormattedText.of(line));
                graphics.guiRenderState.submitText(
                        new SbaTextRenderState(lineFcs, graphics.pose(), x + totalWidth / 2F - mc.font.width(line) / 2F, headerY, -1, 0, true, graphics.scissorStack.peek())
                );
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

                FormattedCharSequence tabLineTextFcs = Language.getInstance().getVisualOrder(FormattedText.of(tabLine.text()));
                if (tabLine.type() == TabStringType.TITLE) {
                    graphics.guiRenderState.submitText(
                            new SbaTextRenderState(tabLineTextFcs, graphics.pose(), (middleX + renderColumn.getMaxWidth() / 2F - tabLine.getWidth() / 2F), middleY, -1, 0, true, graphics.scissorStack.peek())
                    );
                } else {
                    graphics.guiRenderState.submitText(
                            new SbaTextRenderState(tabLineTextFcs, graphics.pose(), middleX, middleY, -1, 0, true, graphics.scissorStack.peek())
                    );
                }
                middleY += LINE_HEIGHT;
                middleX = savedX;
            }

            middleX += renderColumn.getMaxWidth() + COLUMN_SPACING;
        }

        // Draw the footer
        if (footer != null) {
            int footerY = y + totalHeight - footer.size() * LINE_HEIGHT;
            for (String line : footer) {
                FormattedCharSequence lineFcs = Language.getInstance().getVisualOrder(FormattedText.of(line));
                graphics.guiRenderState.submitText(
                        new SbaTextRenderState(lineFcs, graphics.pose(), x + totalWidth / 2F - mc.font.width(line) / 2F, footerY, -1, 0, true, graphics.scissorStack.peek())
                );
                footerY += LINE_HEIGHT;
            }
        }
    }

}