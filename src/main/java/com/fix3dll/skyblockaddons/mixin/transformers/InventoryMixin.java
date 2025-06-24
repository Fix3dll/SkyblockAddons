package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.MinecraftHook;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "setSelectedSlot", at = @At("HEAD"))
    public void sba$onSelectedChange(int selectedHotbarSlot, CallbackInfo ci) {
        MinecraftHook.onUpdateSelectedItem();
    }

}