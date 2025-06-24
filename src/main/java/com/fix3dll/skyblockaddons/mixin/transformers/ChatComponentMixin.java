package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.ChatComponentExtension;
import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatComponentExtension {

    @Shadow protected abstract double screenToChatX(double x);
    @Shadow protected abstract double screenToChatY(double y);
    @Shadow protected abstract int getMessageLineIndexAt(double mouseX, double mouseY);
    @Shadow @Final private List<GuiMessage.Line> trimmedMessages;

    @Override
    public GuiMessageLineExtension sba$getGuiMessageLineAt(double mouseX, double mouseY) {
        double d = this.screenToChatX(mouseX);
        double e = this.screenToChatY(mouseY);
        int i = this.getMessageLineIndexAt(d, e);
        if (i >= 0 && i < this.trimmedMessages.size()) {
            return (GuiMessageLineExtension) (Object) this.trimmedMessages.get(i);
        } else {
            return null;
        }
    }

    @WrapWithCondition(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    public <E> boolean sba$addMessageToDisplayQueue(List<GuiMessage.Line> instance, int i, E e, GuiMessage message) {
        int prevSize = instance.size();
        instance.add(i, ((GuiMessageLineExtension) e).sba$withParentComponent(message.content()));

        return prevSize == instance.size(); // TODO mod compat?
    }

}