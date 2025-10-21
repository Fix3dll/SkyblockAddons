package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;clear()V"))
    private static void sba$clearChromaUniforms(Window window, TracyFrameCapture tracyFrameCapture, CallbackInfo ci) {
        GuiRendererHook.getChromaUniform().endFrame();
    }

}