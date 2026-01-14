package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.mixin.extensions.ChatComponentExtension;
import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ChatScreenHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public static boolean logNextChatComponent = false;

    public static void copyChatMessage(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0) return;
        if (Feature.DEVELOPER_MODE.isDisabled()
                && (Feature.CHAT_MESSAGE_COPYING.isDisabled() || !main.getUtils().isOnSkyblock())) {
            return;
        }

        Window handle = MC.getWindow();
        boolean isLeftControlDown = Util.getPlatform() == Util.OS.OSX
                ? InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SUPER)
                : InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_CONTROL);

        if (isLeftControlDown) {
            ChatComponentExtension extendedChatComponent = (ChatComponentExtension) MC.gui.getChat();
            GuiMessageLineExtension extendedLine = extendedChatComponent.sba$getGuiMessageLineAt(event, isDoubleClick);

            if (extendedLine != null) {
                Component parentComponent;
                String parentComponentString;

                // Warn and log for mod conflicts
                try {
                    parentComponent = extendedLine.sba$getParentComponent();
                    parentComponentString = parentComponent.getString();
                } catch (Exception e) {
                    String failMessage = Translations.getMessage("messages.chatMessageCopying.failed");
                    MC.getToastManager().addToast(SystemToast.multiline(
                            MC,
                            new SystemToast.SystemToastId(3000L),
                            Utils.COMPONENT_TITLE,
                            Component.literal(failMessage).withColor(ColorCode.RED.getColor())
                    ));
                    LOGGER.error(failMessage, e);

                    // See GuiMessageLineMixin#sba$withParentComponent
                    main.getScheduler().scheduleTask(scheduledTask -> {
                        if (MC.player != null) {
                            ChatScreenHook.logNextChatComponent = true;
                            MC.gui.getChat().addMessage(Component.empty());
                        }
                    }, 0);

                    return;
                }

                boolean isLeftShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT);
                if (isLeftShiftDown) {
                    DevUtils.copyStringToClipboard(
                            TextUtils.getFormattedText(parentComponent),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.formatted"),
                            true
                    );
                } else {
                    DevUtils.copyStringToClipboard(
                            TextUtils.stripColor(parentComponentString),
                            ColorCode.GREEN + Translations.getMessage("messages.chatMessageCopying.unformatted"),
                            true
                    );
                }

                // Stop vanilla handling like click actions
                cir.cancel();
            }
        }
    }

}