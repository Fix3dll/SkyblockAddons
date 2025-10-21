package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ContainerScreenHook;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin  {

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", ordinal = 0))
    public void sba$renderBg0(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight, ContainerScreenHook.color());
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", ordinal = 1))
    public void sba$renderBg1(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight, ContainerScreenHook.color());
    }

}