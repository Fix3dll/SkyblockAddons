package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I"), cancellable = true)
    private void sba$rightClickMouse(CallbackInfo ci) {
        ReturnValue<?> returnValue = new ReturnValue<>();
        MinecraftHook.rightClickMouse(returnValue);
        if (returnValue.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I"))
    private void sba$runTick(CallbackInfo ci) {
        MinecraftHook.updatedCurrentItem();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void sba$clickMouse(CallbackInfo ci) {
        ReturnValue<?> returnValue = new ReturnValue<>();
        MinecraftHook.onClickMouse(returnValue);
        if (returnValue.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    private void sba$sendClickBlockToController(boolean leftClick, CallbackInfo ci) {
        ReturnValue<?> returnValue = new ReturnValue<>();
        MinecraftHook.onSendClickBlockToController(leftClick, returnValue);
        if (returnValue.isCancelled()) {
            ci.cancel();
        }
    }
}
