package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import com.fix3dll.skyblockaddons.mixin.hooks.ChatScreenHook;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements GuiMessageLineExtension {

    @Unique
    private Component sba$parentComponent = null;

    @Unique
    private final Logger sba$LOGGER = SkyblockAddons.getLogger();

    @Override
    public Component sba$getParentComponent() {
        return sba$parentComponent;
    }

    @Override
    public GuiMessage.Line sba$withParentComponent(Component chatComponent) {
        this.sba$parentComponent = chatComponent;

        if (ChatScreenHook.logNextChatComponent) {
            sba$LOGGER.warn("sba$withParentComponent dump", new Exception("sba$withParentComponent dump"));
            ChatScreenHook.logNextChatComponent = false;
        }

        return (GuiMessage.Line) (Object) this;
    }

}