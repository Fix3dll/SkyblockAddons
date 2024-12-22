package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.EffectRendererHook;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderer.class)
public class EffectRendererMixin {

    @Inject(method = "addEffect", at = @At("HEAD"))
    private void sba$addEffect(EntityFX effect, CallbackInfo ci) {
        EffectRendererHook.onAddParticle(effect);
    }

    /*
     * Insert {@link codes.biscuit.skyblockaddons.asm.hooks.EffectRendererHook#renderParticleOverlays(float)} right before the last call to depthMask(true).
     */
    @Inject(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;depthMask(Z)V", ordinal = 2))
    private void sba$renderParticles(Entity entityIn, float partialTicks, CallbackInfo ci) {
        EffectRendererHook.renderParticleOverlays(partialTicks);
    }
}
