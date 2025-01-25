package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHelper.class)
public class MouseHelperMixin {

    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private void sba$ungrabMouseCursor(CallbackInfo ci) {
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isEnabled() && !SkyblockAddons.getInstance().getPlayerListener().shouldResetMouse()) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }
}
