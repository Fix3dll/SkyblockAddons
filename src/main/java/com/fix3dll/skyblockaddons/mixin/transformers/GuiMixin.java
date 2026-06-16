package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.ClientEvents;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0, opcode = Opcodes.GETFIELD), cancellable = true)
    public void sba$beforeScreenInit(Screen screen, CallbackInfo ci) {
        if (ClientEvents.BEFORE_SET_SCREEN.invoker().beforeSetScreen(screen)) {
            ci.cancel();
        }
    }

}