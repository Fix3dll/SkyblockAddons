package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.RenderEndermanHook;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEnderman.class)
public class RenderEndermanMixin {

    @Final
    @Shadow
    private static ResourceLocation endermanTextures;

    @Inject(method = "getEntityTexture", at = @At("RETURN"), cancellable = true)
    private void sba$getEntityTexture_RenderEnderman(EntityEnderman entityEnderman, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(RenderEndermanHook.getEndermanTexture(endermanTextures));
    }
}
