package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.GuiScreenHook;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public class GuiScreenMixin {

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    private void sba$renderToolTip(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (GuiScreenHook.onRenderTooltip(stack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleComponentClick", at = @At("HEAD"))
    private void sba$handleComponentClick(IChatComponent component, CallbackInfoReturnable<Boolean> cir) {
        GuiScreenHook.handleComponentClick(component);
    }
}
