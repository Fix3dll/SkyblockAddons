package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.mixin.extensions.ChatComponentExtension;
import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;getComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"))
    public void sba$mouseClicked(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir, @Local ChatComponent chatComponent) {
        if (event.button() != 0) return;
        if (Feature.DEVELOPER_MODE.isDisabled()
                && (Feature.CHAT_MESSAGE_COPYING.isDisabled() || !SkyblockAddons.getInstance().getUtils().isOnSkyblock())) {
            return;
        }

        Window handle = Minecraft.getInstance().getWindow();
        boolean isLeftControlDown = Util.getPlatform() == Util.OS.OSX
                ? InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SUPER)
                : InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_CONTROL);

        if (isLeftControlDown) {
            ChatComponentExtension extendedChatComponent = (ChatComponentExtension) (Object) chatComponent;
            GuiMessageLineExtension extendedLine = extendedChatComponent.sba$getGuiMessageLineAt(event, isDoubleClick);

            if (extendedLine != null) {
                boolean isLeftShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT);
                if (isLeftShiftDown) {
                    DevUtils.copyStringToClipboard(
                            TextUtils.getFormattedText(extendedLine.sba$getParentComponent()),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.formatted")
                    );
                } else {
                    DevUtils.copyStringToClipboard(
                            TextUtils.stripColor(extendedLine.sba$getParentComponent().getString()),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.unformatted")
                    );
                }
            }
        }
    }

}