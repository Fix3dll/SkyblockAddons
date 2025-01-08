package codes.biscuit.skyblockaddons.mixins.transformers;

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
        MinecraftHook.rightClickMouse(ci);
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I"))
    private void sba$runTick(CallbackInfo ci) {
        MinecraftHook.onUpdateCurrentItem();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void sba$clickMouse(CallbackInfo ci) {
        MinecraftHook.onClickMouse(ci);
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    private void sba$sendClickBlockToController(boolean leftClick, CallbackInfo ci) {
        MinecraftHook.onSendClickBlockToController(leftClick, ci);
    }
}
