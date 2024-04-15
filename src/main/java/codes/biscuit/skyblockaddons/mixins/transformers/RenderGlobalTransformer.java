package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.RenderGlobalHook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * See {@link RenderGlobalHook#blockRenderingSkyblockItemOutlines(ICamera, float, double, double, double)},
 * {@link RenderGlobalHook#afterFramebufferDraw()}, {@link RenderGlobalHook#onAddBlockBreakParticle(int, BlockPos, int)}, and
 * {@link RenderGlobalHook#shouldRenderSkyblockItemOutlines()})
 */
@Mixin(RenderGlobal.class)
public class RenderGlobalTransformer {

    @ModifyExpressionValue(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    private boolean renderEntities(boolean original, Entity renderViewEntity, ICamera camera, float partialTicks) {
        return RenderGlobalHook.blockRenderingSkyblockItemOutlines(camera, partialTicks, renderViewEntity.posX, renderViewEntity.posY, renderViewEntity.posZ) && original;
    }

    @Inject(method = "isRenderEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void isRenderEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (RenderGlobalHook.shouldRenderSkyblockItemOutlines()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderEntityOutlineFramebuffer", at = @At("RETURN"))
    private void renderEntityOutlineFramebuffer(CallbackInfo ci) {
        RenderGlobalHook.afterFramebufferDraw();
    }

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    private void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        RenderGlobalHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }
}
