package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.ChatComponentExtension;
import com.fix3dll.skyblockaddons.mixin.extensions.GuiMessageLineExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatComponentExtension {

    @Shadow public abstract boolean isChatFocused();
    @Shadow protected abstract int getWidth();
    @Shadow protected abstract double getScale();
    @Shadow @Final public List<GuiMessage.Line> trimmedMessages;
    @Shadow public abstract int getLinesPerPage();
    @Shadow private int chatScrollbarPos;
    @Shadow @Final private Minecraft minecraft;
    @Shadow protected abstract int getLineHeight();

    @Override
    public GuiMessageLineExtension sba$getGuiMessageLineAt(MouseButtonEvent event, boolean isDoubleClick) {
        double x = event.x() / this.getScale() - 4.0;
        double y = this.minecraft.getWindow().getGuiScaledHeight() - event.y() - 40.0;
        y /= (this.getScale() * this.getLineHeight());

        if (this.isChatFocused()) {
            if (!(x < -4.0) && !(x > Mth.floor(this.getWidth() / this.getScale()))) {
                int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (y >= 0.0 && y < i) {
                    int j = Mth.floor(y + this.chatScrollbarPos);
                    if (j >= 0 && j < this.trimmedMessages.size()) {
                        return (GuiMessageLineExtension) (Object) this.trimmedMessages.get(j);
                    }
                }
            }
        }
        return null;
    }

    @WrapOperation(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V"))
    private static <E> void sba$addMessageToDisplayQueue(List<GuiMessage.Line> instance, E e, Operation<Void> original, GuiMessage message) {
        // TODO mod compat?
        original.call(instance, ((GuiMessageLineExtension) e).sba$withParentComponent(message.content()));
    }

}