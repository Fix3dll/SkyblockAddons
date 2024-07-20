package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.EntityRendererHook;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererTransformer {

    @Inject(method = "getNightVisionBrightness", at = @At("HEAD"), cancellable = true)
    private void getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, CallbackInfoReturnable<Float> cir) {
        ReturnValue<Float> returnValue = new ReturnValue<>();
        EntityRendererHook.onGetNightVisionBrightness(returnValue);
        if (returnValue.isCancelled()) {
            cir.setReturnValue(1.0F);
        }
    }
}
