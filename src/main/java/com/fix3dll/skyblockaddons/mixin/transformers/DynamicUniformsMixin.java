package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import net.minecraft.client.renderer.DynamicGpuData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicGpuData.class)
public class DynamicUniformsMixin {

    @Inject(method = "reset", at = @At("HEAD"))
    private static void sba$resetChromaUniforms(CallbackInfo ci) {
        GuiRendererHook.getChromaUniform().endFrame();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private static void sba$closeChromaUniforms(CallbackInfo ci) {
        GuiRendererHook.getChromaUniform().close();
    }

}