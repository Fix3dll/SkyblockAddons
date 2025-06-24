package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.DisconnectedScreenHook;
import com.fix3dll.skyblockaddons.mixin.hooks.PauseScreenHook;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void sba$onDisconnect(CallbackInfo ci) {
        DisconnectedScreenHook.onDisconnect();
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sba$addMenuButtons(CallbackInfo ci, GridLayout gridLayout, GridLayout.RowHelper rowHelper) {
        PauseScreenHook.addMenuButtons(rowHelper);
    }

}