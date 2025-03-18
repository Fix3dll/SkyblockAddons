package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Button that lets the user select one item in a given set of items.
 */
public class ButtonCycling extends SkyblockAddonsButton {

    private static final ResourceLocation ARROW_LEFT = new ResourceLocation("skyblockaddons", "gui/flatarrowleft.png");
    private static final ResourceLocation ARROW_RIGHT = new ResourceLocation("skyblockaddons", "gui/flatarrowright.png");

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
        super(0, x, y, "");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Item list must have at least one element.");
        } else if (callback == null) {
            throw new IllegalArgumentException("ButtonCycling's callback cannot be null!");
        }

        this.priority = 1001;
        this.textWidth = width - (2 * height) - 6; // 2 * 3 text padding on both sides
        this.width = width;
        this.height = height;
        this.itemList = items;
        this.index = selectedIndex > 0 && selectedIndex < itemList.size() ? selectedIndex : 0;
        this.callback = callback;
        this.isSettingsGui = Minecraft.getMinecraft().currentScreen instanceof SettingsGui;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        int color = main.getUtils().getDefaultColor(100);
        int leftColor = main.getUtils().getDefaultColor(isOverLeftButton(mouseX, mouseY) ? 200 : 90);
        int rightColor = main.getUtils().getDefaultColor(isOverRightButton(mouseX, mouseY) ? 200 : 90);

        String name = itemList.get(index).getDisplayName();
        String trimmedName = mc.fontRendererObj.trimStringToWidth(name, textWidth);
        if (!name.equals(trimmedName)) {
            trimmedName = ellipsize(trimmedName);
        }
        String description = itemList.get(index).getDescription();
        // background / text area
        GlStateManager.enableBlend();
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color);
        // left button
        drawRect(xPosition, yPosition, xPosition + height, yPosition + height, leftColor);
        //right button
        drawRect(xPosition + width - height, yPosition, xPosition + width, yPosition + height, rightColor);

        // inside text
        drawCenteredString(mc.fontRendererObj, trimmedName, xPosition + width / 2, yPosition + height / 4, ColorCode.WHITE.getColor());

        GlStateManager.color(1, 1, 1, 1);

        // Arrow buttons are square so width = height
        mc.getTextureManager().bindTexture(ARROW_LEFT);
        //noinspection SuspiciousNameCombination
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, height, height, height, height, true);

        mc.getTextureManager().bindTexture(ARROW_RIGHT);
        //noinspection SuspiciousNameCombination
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition + width - height, yPosition, 0, 0, height, height, height, height, true);

        if (!name.equals(trimmedName)) {
            if (isOverText(mouseX, mouseY)) {
                // draw tooltip next to the cursor showing the full title
                final int stringWidth = mc.fontRendererObj.getStringWidth(name);
                int rectLeft = mouseX + 3;
                int rectTop = mouseY + 3;
                int rectRight = rectLeft + stringWidth + 8;
                int rectBottom = rectTop + 12;
                drawRect(rectLeft, rectTop, rectRight, rectBottom, ColorCode.BLACK.getColor());
                mc.fontRendererObj.drawString(name, rectLeft + 4, rectTop+2, ColorCode.WHITE.getColor());
            }
        }

        // description
        if (description != null) {
            if (isSettingsGui) {
                drawCenteredString(mc.fontRendererObj, description, xPosition + width / 2, yPosition + height + 2, ColorCode.GRAY.getColor());
            } else if (isOverText(mouseX, mouseY)) {
                int descWidth = mc.fontRendererObj.getStringWidth(description);
                DrawUtils.drawHoveringText(Collections.singletonList(description), mouseX, mouseY, mc.currentScreen.width, mc.currentScreen.height, descWidth);
            }
        }
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (isOverLeftButton(mouseX, mouseY)) {
            index = index == itemList.size() - 1 ? 0 : index + 1;
            callback.accept(index);
            return true;
        } else if (isOverRightButton(mouseX, mouseY)) {
            index = index == 0 ? itemList.size() - 1 : index - 1;
            callback.accept(index);
            return true;
        }
        return false;
    }

    private boolean isOverText(int mouseX, int mouseY) {
        return mouseX > xPosition + height
                && mouseX < xPosition + width - height
                && mouseY > yPosition
                && mouseY < yPosition + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the left arrow button
     */
    private boolean isOverLeftButton(int mouseX, int mouseY) {
        return mouseX > xPosition
                && mouseX < xPosition + height
                && mouseY > yPosition
                && mouseY < yPosition + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the right arrow button
     */
    private boolean isOverRightButton(int mouseX, int mouseY) {
        return mouseX > xPosition + width - height
                && mouseX < xPosition + width
                && mouseY > yPosition
                && mouseY < yPosition + height;
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