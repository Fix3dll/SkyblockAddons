package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public abstract class SkyblockAddonsButton extends GuiButton {
    protected static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final int FADE_MILLIS = 150;

    // Used to calculate the transparency when fading in.
    private final long timeOpened = System.currentTimeMillis();

    public SkyblockAddonsButton(int buttonId, int x, int y, String buttonText) {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public SkyblockAddonsButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return isHovered(mouseX, mouseY, 1);
    }

    public boolean isHovered(int mouseX, int mouseY, float scale) {
        return isHovered(xPosition, yPosition, mouseX, mouseY, scale);
    }

    public boolean isHovered(float x, float y, int mouseX, int mouseY, float scale) {
        return mouseX >= x * scale && mouseX < x * scale + this.width * scale
                && mouseY >= y * scale && mouseY < y * scale + this.height * scale;
    }

    public float calculateAlphaMultiplier() {
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            if (timeSinceOpen <= FADE_MILLIS) {
                return (float) timeSinceOpen / FADE_MILLIS;
            }
        }
        return 1.0F;
    }

    public void drawButtonBoxAndText(int boxColor, float scale, int fontColor) {
        drawButtonBoxAndText(displayString, xPosition, yPosition, width, height, boxColor, scale, fontColor);
    }

    public static void drawButtonBoxAndText(String displayString, int x, int y, int width, int height, int boxColor, float scale, int fontColor) {
        ButtonColorBox.drawColorRect(x, y, x + width, y + height, boxColor);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        //noinspection IntegerDivisionInFloatingPointContext
        DrawUtils.drawCenteredText(
                displayString,
                ((x + width / 2) / scale),
                ((y + (height - (8 * scale)) / 2) / scale),
                fontColor
        );
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }
}
