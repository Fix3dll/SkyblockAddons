package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.mixins.hooks.FontRendererHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.hooks.FontRendererHook")
public class PatcherFontRendererTransformer {

    @Inject(method = "renderStringAtPos(Ljava/lang/String;Z)Z", at = @At("HEAD"), cancellable = true)
    public void overridePatcherFontRenderer(String string, boolean shadow, CallbackInfoReturnable<Boolean> cir) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.TURN_ALL_FEATURES_CHROMA)
                || FontRendererHook.shouldManuallyRecolorFont()) {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }
}