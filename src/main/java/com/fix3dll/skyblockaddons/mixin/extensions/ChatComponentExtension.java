package com.fix3dll.skyblockaddons.mixin.extensions;

import net.minecraft.client.input.MouseButtonEvent;

public interface ChatComponentExtension {

    GuiMessageLineExtension sba$getGuiMessageLineAt(MouseButtonEvent event, boolean isDoubleClick);

}