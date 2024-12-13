package codes.biscuit.skyblockaddons.gui.buttons;

import net.minecraft.client.Minecraft;

public class ButtonText extends SkyblockAddonsButton {

    private final boolean centered;
    private final int color;

    public ButtonText(int x, int y, String text, boolean centered, int color) {
        super(0, x, y, text);
        this.centered = centered;
        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        int x = xPosition;
        int y = yPosition;

        if (centered) {
            x -= mc.fontRendererObj.getStringWidth(displayString) / 2;
        }

        mc.fontRendererObj.drawString(displayString, x, y, color);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }
}
