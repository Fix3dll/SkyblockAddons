package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.mixins.extensions.ChatLineExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;

public class GuiChatHook extends Gui {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final GuiNewChat guiNewChat = MC.ingameGUI.getChatGUI();

    public static IChatComponent getParentComponent(int mouseX, int mouseY) {
        if (guiNewChat.getChatOpen()) {
            ScaledResolution scaledResolution = new ScaledResolution(MC);
            int i = scaledResolution.getScaleFactor();
            float f = guiNewChat.getChatScale();
            int j = mouseX / i - 3;
            int k = mouseY / i - 27;
            j = MathHelper.floor_float((float) j / f);
            k = MathHelper.floor_float((float) k / f);
            if (j >= 0 && k >= 0) {
                int l = Math.min(guiNewChat.getLineCount(), guiNewChat.drawnChatLines.size());
                if (j <= MathHelper.floor_float((float) guiNewChat.getChatWidth() / guiNewChat.getChatScale())
                        && k < MC.fontRendererObj.FONT_HEIGHT * l + l) {
                    int m = k / MC.fontRendererObj.FONT_HEIGHT + guiNewChat.scrollPos;
                    if (m >= 0 && m < guiNewChat.drawnChatLines.size()) {
                        return ((ChatLineExtension) guiNewChat.drawnChatLines.get(m)).sba$getParentComponent();
                    }
                }
            }
        }
        return null;
    }

}