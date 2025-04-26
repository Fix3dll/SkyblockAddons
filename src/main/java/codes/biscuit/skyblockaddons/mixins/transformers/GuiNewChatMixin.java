package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.extensions.ChatLineExtension;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiNewChatHook;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {

    @Redirect(method = "printChatMessageWithOptionalDeletion", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IChatComponent;getUnformattedText()Ljava/lang/String;"))
    private String sba$printChatMessageWithOptionalDeletion(IChatComponent chatComponent) {
        return GuiNewChatHook.getUnformattedText(chatComponent);
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    private <E> void sba$setChatLine(List<ChatLine> instance, int i, E e, IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        instance.add(i, ((ChatLineExtension) e).sba$withParentComponent(chatComponent));
    }

}