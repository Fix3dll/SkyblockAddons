package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

/**
 * Button that lets the user select one item in a given set of items.
 */
public class ButtonCycling extends SkyblockAddonsButton {

    private static final ResourceLocation ARROW_LEFT = SkyblockAddons.resourceLocation("gui/flatarrowleft.png");
    private static final ResourceLocation ARROW_RIGHT = SkyblockAddons.resourceLocation("gui/flatarrowright.png");

    /**
     * Item that can be used in this Select button
     */
    public interface SelectItem {

        /**
         * @return A name displayed inside the button
         */
        String getDisplayName();

        /**
         * @return A description displayed below the button
         */
        String getDescription();
    }

    private final List<SelectItem> itemList;
    private int index;

    private final int textWidth;
    private final Consumer<Integer> callback;
    private final boolean isSettingsGui;

    /*
     * Rough sketch of the button
     *  __ __________ __
     * |< |          |> |
     *  -- ---------- --
     */
    /**
     * Create a new Select button at (x, y) with a given width and height and set of items to select from.
     * Initially selects the given {@code selectedIndex} or {@code 0} if that is out of bounds of the given list.
     * Optionally accept a callback that is called whenever a new item is selected.
     * Note: Effective width for text is about {@code width - 2 * height} as the arrow buttons are squares with
     * a side length of {@code height}.
     * Text will be trimmed and marked with ellipses {@code …} if it is too long to fit in the text area.
     *
     * @param x             x position
     * @param y             y position
     * @param width         total width
     * @param height        height
     * @param items         non-null and non-empty List of items to choose from
     * @param selectedIndex initially selected index in the given list of items
     * @param callback      Nullable callback when a new item is selected
     */
    public ButtonCycling(int x, int y, int width, int height, List<SelectItem> items, int selectedIndex, Consumer<Integer> callback) {
        super(x, y, Component.empty());
        if(items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Item list must have at least one element.");
        } else if (callback == null) {
            throw new IllegalArgumentException("ButtonCycling's callback cannot be null!");
        }

        this.textWidth = width - (2 * height) - 6; // 2 * 3 text padding on both sides
        this.width = width;
        this.height = height;
        this.itemList = items;
        this.index = selectedIndex > 0 && selectedIndex < itemList.size() ? selectedIndex : 0;
        this.callback = callback;
        this.isSettingsGui = Minecraft.getInstance().screen instanceof SettingsGui;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int leftColor = main.getUtils().getDefaultColor(isOverLeftButton(mouseX, mouseY) ? 200 : 90);
        int rightColor = main.getUtils().getDefaultColor(isOverRightButton(mouseX, mouseY) ? 200 : 90);

        String name = itemList.get(index).getDisplayName();
        String trimmedName = MC.font.plainSubstrByWidth(name, textWidth);
        if (!name.equals(trimmedName)) {
            trimmedName = ellipsize(trimmedName);
        }
        String description = itemList.get(index).getDescription();
        // background / text area
        graphics.fill(getX(), getY(), getX() + width, getY() + height, main.getUtils().getDefaultColor(100));
        // left button
        graphics.fill(getX(), getY(), getX() + height, getY() + height, leftColor);
        //right button
        graphics.fill(getX() + width - height, getY(), getX() + width, getY() + height, rightColor);

        // inside text
        graphics.drawCenteredString(MC.font, trimmedName,  getX() + width / 2, getY() + height / 4, ColorCode.WHITE.getColor());

        // Arrow buttons are square so width = height
        //noinspection SuspiciousNameCombination
        graphics.blit(RenderType::guiTextured, ARROW_LEFT, getX(), getY(), 0, 0, height, height, height, height, -1);

        //noinspection SuspiciousNameCombination
        graphics.blit(RenderType::guiTextured, ARROW_RIGHT, getX() + width - height, getY(), 0, 0, height, height, height, height, -1);

        if (!name.equals(trimmedName)) {
            if (isOverText(mouseX, mouseY)) {
                // draw tooltip next to the cursor showing the full title
                final int stringWidth = MC.font.width(name);
                int rectLeft = mouseX + 3;
                int rectTop = mouseY + 3;
                int rectRight = rectLeft + stringWidth + 8;
                int rectBottom = rectTop + 12;
                graphics.fill(rectLeft, rectTop, rectRight, rectBottom, ColorCode.BLACK.getColor());
                graphics.drawString(MC.font, name, rectLeft + 4, rectTop + 2, ColorCode.WHITE.getColor(), false);
            }
        }

        // description
        if (description != null) {
            if (isSettingsGui) {
               graphics.drawCenteredString(MC.font, description, getX() + width / 2, getY() + height + 2, ColorCode.GRAY.getColor());
            } else if (isOverText(mouseX, mouseY)) {
                graphics.renderTooltip(MC.font, Component.literal(description), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOverLeftButton(mouseX, mouseY)) {
            index = index == itemList.size() - 1 ? 0 : index + 1;
            callback.accept(index);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        } else if (isOverRightButton(mouseX, mouseY)) {
            index = index == 0 ? itemList.size() - 1 : index - 1;
            callback.accept(index);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    private boolean isOverText(double mouseX, double mouseY) {
        return mouseX > getX() + height
                && mouseX < getX() + width - height
                && mouseY > getY()
                && mouseY < getY() + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the left arrow button
     */
    private boolean isOverLeftButton(double mouseX, double mouseY) {
        return mouseX > getX()
                && mouseX < getX() + height
                && mouseY > getY()
                && mouseY < getY() + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the right arrow button
     */
    private boolean isOverRightButton(double mouseX, double mouseY) {
        return mouseX > getX() + width - height
                && mouseX < getX() + width
                && mouseY > getY()
                && mouseY < getY() + height;
    }

    /**
     * Replaces the last character in the given string with the ellipses character {@code …}
     * @param text Text to ellipsize
     * @return Input text with … at the end
     */
    private String ellipsize(String text) {
        return new StringBuilder(text)
                .replace(text.length() - 1, text.length(), "…")
                .toString();
    }

}