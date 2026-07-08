package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.LevelRendererHook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyExpressionValue(method = "submitEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;haveGlowingEntities:Z", opcode = Opcodes.GETFIELD))
    public boolean sba$submitEntities(boolean original, @Local(name = "state") EntityRenderState entityRenderState) {
        if (original) {
            EntityOutlineRenderer.colorSkyblockEntityOutlines(entityRenderState);
        }
        return original;
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    public void sba$destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelRendererHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }

}