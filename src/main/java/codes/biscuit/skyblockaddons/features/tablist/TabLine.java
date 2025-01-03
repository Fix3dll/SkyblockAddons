package codes.biscuit.skyblockaddons.features.tablist;

import lombok.Getter;
import net.minecraft.client.Minecraft;

@Getter
public class TabLine {

    private final TabStringType type;
    private final String text;

    public TabLine(String text, TabStringType type) {
        this.type = type;
        this.text = text;
    }

    public int getWidth() {
        int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);

        if (type == TabStringType.PLAYER) {
            width += 8 + 2; // Player head
        }

        if (type == TabStringType.TEXT) {
            width += 4; // Space is 4
        }

        return width;
    }
}