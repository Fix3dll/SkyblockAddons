package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.PlayerControllerMPHook;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {

    @Inject(method = "onPlayerDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playAuxSFX(ILnet/minecraft/util/BlockPos;I)V"))
    private void sba$onPlayerDestroyBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        PlayerControllerMPHook.onPlayerDestroyBlock(pos);
    }

    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    private void sba$windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        ReturnValue<ItemStack> returnValue = new ReturnValue<>();
        PlayerControllerMPHook.onWindowClick(slotId, mouseButtonClicked, mode, playerIn, returnValue);
        if (returnValue.isCancelled()) {
            cir.setReturnValue(returnValue.getReturnValue());
        }
    }

    @Inject(method = "resetBlockRemoving", at = @At("HEAD"))
    private void sba$resetBlockRemoving(CallbackInfo ci) {
        PlayerControllerMPHook.onResetBlockRemoving();
    }
}
