package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.LevelRendererHook;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyArgs(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
    public void sba$renderEntities(Args args, @Local Entity entity) {
       EntityOutlineRenderer.colorSkyblockEntityOutlines(args, entity);
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    public void sba$destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelRendererHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }

}