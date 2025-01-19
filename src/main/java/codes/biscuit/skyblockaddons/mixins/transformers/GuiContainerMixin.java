package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiContainerHook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin {

    @Shadow private Slot theSlot;

    @Inject(method = "drawScreen", at = @At(value = "CONSTANT", args = "intValue=240", ordinal = 1, shift = At.Shift.AFTER))
    private void sba$setLastSlot(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    private void sba$drawGradientRect(GuiContainer instance, int left, int top, int right, int bottom, int startColor, int endColor) {
        GuiContainerHook.drawGradientRect(instance, left, top, right, bottom, startColor, endColor, this.theSlot);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawSlot(Lnet/minecraft/inventory/Slot;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sba$drawSlotLocked(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, int i, int j, int k, int l, int i1, Slot slot) {
        GuiContainerHook.drawSlot(slot);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void sba$drawBackpacks(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ContainerPreviewManager.drawContainerPreviews((GuiContainer) (Object) this, mouseX, mouseY);
    }

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;checkHotbarKeys(I)Z", shift = At.Shift.BY, by = -2), cancellable = true)
    private void sba$keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        GuiContainerHook.onKeyInput(this.theSlot, keyCode, ci);
    }

    @Inject(method = "mouseClicked", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;ignoreMouseUp:Z", ordinal = 0), cancellable = true)
    private void sba$mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        GuiContainerHook.onKeyInput(this.theSlot, Mouse.getEventButton() - 100, ci);
    }

    @Inject(method = "handleMouseClick", at = @At("HEAD"), cancellable = true)
    private void sba$handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (GuiContainerHook.onHandleMouseClick(slotIn, slotId, clickedButton, clickType))
            ci.cancel();
    }
}