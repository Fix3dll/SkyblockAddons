package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.GuiContainerHook;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin {

    @Shadow private Slot theSlot;

    @Shadow protected abstract void drawSlot(Slot slotIn);

    @Inject(method = "drawScreen", at = @At(value = "CONSTANT", args = "intValue=240", ordinal = 1, shift = At.Shift.AFTER))
    private void sba$setLastSlot(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainerHook.setLastSlot();
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    private void sba$drawGradientRect(GuiContainer instance, int left, int top, int right, int bottom, int startColor, int endColor) {
        GuiContainerHook.drawGradientRect(instance, left, top, right, bottom, startColor, endColor, this.theSlot);
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawSlot(Lnet/minecraft/inventory/Slot;)V"))
    private void sba$GuiContainerHook_drawSlot(GuiContainer instance, Slot slot) {
        this.drawSlot(slot);
        GuiContainerHook.drawSlot((GuiContainer) (Object) this, slot);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void sba$drawBackpacks(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainerHook.drawBackpacks((GuiContainer) (Object) this, mouseX, mouseY);
    }

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;checkHotbarKeys(I)Z", shift = At.Shift.BY, by = -2), cancellable = true)
    private void sba$keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        ReturnValue<?> returnValue = new ReturnValue<>();
        GuiContainerHook.keyTyped((GuiContainer) (Object) this, keyCode, this.theSlot, returnValue);
        if (returnValue.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void keyTyped2(char typedChar, int keyCode, CallbackInfo ci) {
        GuiContainerHook.keyTyped(keyCode);
    }

    @Inject(method = "handleMouseClick", at = @At("HEAD"), cancellable = true)
    private void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (GuiContainerHook.onHandleMouseClick(slotIn, slotId, clickedButton, clickType))
            ci.cancel();
    }
}
