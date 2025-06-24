package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements GuiMessageLineExtension {

    @Unique
    private Component sba$parentComponent = null;

    @Override
    public Component sba$getParentComponent() {
        return sba$parentComponent;
    }

    @Override
    public GuiMessage.Line sba$withParentComponent(Component chatComponent) {
        this.sba$parentComponent = chatComponent;
        return (GuiMessage.Line) (Object) this;
    }

}