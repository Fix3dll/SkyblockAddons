package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.RenderGlobalHook;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * See {@link RenderGlobalHook#afterFramebufferDraw()}, {@link RenderGlobalHook#onAddBlockBreakParticle(int, BlockPos, int)},
 * and {@link RenderGlobalHook#shouldRenderSkyblockItemOutlines()})
 * </p>
 * For Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z see {@link codes.biscuit.skyblockaddons.asm.transformer.RenderGlobalTransformer}
 */
@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {

    @Inject(method = "isRenderEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void sba$isRenderEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (RenderGlobalHook.shouldRenderSkyblockItemOutlines()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderEntityOutlineFramebuffer", at = @At("RETURN"))
    private void sba$renderEntityOutlineFramebuffer(CallbackInfo ci) {
        RenderGlobalHook.afterFramebufferDraw();
    }

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    private void sba$sendBlockBreakProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        RenderGlobalHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }
}
