package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ContainerScreenHook;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin  {

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0))
    public void sba$renderBg0(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight, ContainerScreenHook.color());
    }

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 1))
    public void sba$renderBg1(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight, ContainerScreenHook.color());
    }

}