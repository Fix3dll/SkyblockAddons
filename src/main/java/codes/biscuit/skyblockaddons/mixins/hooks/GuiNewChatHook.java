package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import net.minecraft.util.IChatComponent;

public class GuiNewChatHook {

    public static String getUnformattedText(IChatComponent iChatComponent) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null && Feature.DEVELOPER_MODE.isEnabled()) {
            return iChatComponent.getFormattedText(); // For logging colored messages...
        }
        return iChatComponent.getUnformattedText();
    }
}
