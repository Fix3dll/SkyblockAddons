package com.fix3dll.skyblockaddons.mixin.extensions;

import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;

public interface GuiMessageLineExtension {

    Component sba$getParentComponent();

    GuiMessage.Line sba$withParentComponent(Component chatComponent);

}