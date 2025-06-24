package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.MultiPlayerGameModeHook;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {


    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public void sba$destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MultiPlayerGameModeHook.onDestroyBlock(pos);
    }

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    public void sba$handleInventoryMouseClick(int containerId, int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {
        MultiPlayerGameModeHook.handleInventoryMouseClick(slotId, mouseButton, clickType, player, ci);
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    public void sba$stopDestroyBlock(CallbackInfo ci) {
        MultiPlayerGameModeHook.onStopDestroyBlock();
    }

}