package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.GuiNewChatHook;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {

    @Redirect(method = "printChatMessageWithOptionalDeletion", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IChatComponent;getUnformattedText()Ljava/lang/String;"))
    private String sba$printChatMessageWithOptionalDeletion(IChatComponent chatComponent) {
        return GuiNewChatHook.getUnformattedText(chatComponent);
    }
}
