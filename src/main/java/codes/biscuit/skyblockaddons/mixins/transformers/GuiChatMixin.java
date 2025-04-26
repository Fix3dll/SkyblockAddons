package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiChatHook;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class GuiChatMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    protected void sba$mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton != 0) return;
        if (Feature.DEVELOPER_MODE.isDisabled()
                && (Feature.CHAT_MESSAGE_COPYING.isDisabled() || !SkyblockAddons.getInstance().getUtils().isOnSkyblock())) {
            return;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            IChatComponent chatComponent = GuiChatHook.getParentComponent(
                    Mouse.getX(), Mouse.getY()
            );

            if (chatComponent != null) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    DevUtils.copyStringToClipboard(
                            chatComponent.getFormattedText(),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.formatted")
                    );
                } else {
                    DevUtils.copyStringToClipboard(
                            TextUtils.stripColor(chatComponent.getUnformattedText()),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.unformatted")
                    );
                }
            }
        }
    }

}