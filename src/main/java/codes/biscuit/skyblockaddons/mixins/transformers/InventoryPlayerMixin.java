package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.MinecraftHook;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerMixin {

    @Inject(method = "changeCurrentItem", at = @At("HEAD"))
    private void sba$changeCurrentItem(int direction, CallbackInfo ci) {
        MinecraftHook.updatedCurrentItem();
    }
}
