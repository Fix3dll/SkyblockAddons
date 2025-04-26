package codes.biscuit.skyblockaddons.mixins.extensions;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

public interface ChatLineExtension {

    IChatComponent sba$getParentComponent();

    ChatLine sba$withParentComponent(IChatComponent chatComponent);

}