package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    static Minecraft instance;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void sba$afterInitializeClient(GameConfig runArgs, CallbackInfo ci) {
        ClientEvents.AFTER_INITIALIZATION.invoker().afterInitializeClient(instance);
    }

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0), cancellable = true)
    public void sba$beforeScreenInit(Screen screen, CallbackInfo ci) {
        if (ClientEvents.BEFORE_SET_SCREEN.invoker().beforeSetScreen(screen)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    public void sba$handleKeybinds(CallbackInfo ci) {
        ClientEvents.HANDLE_KEYBINDS.invoker().handleKeybinds();
    }

    @Inject(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I"), cancellable = true)
    private void sba$rightClickMouse(CallbackInfo ci) {
        MinecraftHook.rightClickMouse(ci);
    }

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    public void sba$updateCurrentItem(CallbackInfo ci) {
        MinecraftHook.onUpdateSelectedItem();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void sba$clickMouse(CallbackInfoReturnable<Boolean> cir) {
        MinecraftHook.onClickMouse(cir);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void sba$sendClickBlockToController(boolean leftClick, CallbackInfo ci) {
        MinecraftHook.onSendClickBlockToController(leftClick, ci);
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    public void sba$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntityOutlineRenderer.shouldRenderEntityOutlines(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "disconnectFromWorld", at = @At("HEAD"))
    public void sba$disconnectFromWorld(Component reason, CallbackInfo ci) {
        MinecraftHook.onDisconnectFromWorld();
    }

}