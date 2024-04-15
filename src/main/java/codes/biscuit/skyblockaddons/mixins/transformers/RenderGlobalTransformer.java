package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.RenderGlobalHook;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * See {@link RenderGlobalHook#blockRenderingSkyblockItemOutlines(ICamera, float, double, double, double)},
 * {@link RenderGlobalHook#afterFramebufferDraw()}, {@link RenderGlobalHook#onAddBlockBreakParticle(int, BlockPos, int)}, and
 * {@link RenderGlobalHook#shouldRenderSkyblockItemOutlines()})
 */
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalTransformer {

    @Shadow protected abstract boolean isRenderEntityOutlines();

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    private boolean renderEntities(RenderGlobal instance, Entity renderViewEntity, ICamera camera, float partialTicks) {
        return RenderGlobalHook.blockRenderingSkyblockItemOutlines(camera, partialTicks, renderViewEntity.posX, renderViewEntity.posY, renderViewEntity.posZ)
                && isRenderEntityOutlines();
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
