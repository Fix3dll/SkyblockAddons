package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.ItemHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "showDurabilityBar", at = @At("HEAD"), cancellable = true, remap = false)
    private void sba$showDurabilityBar(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemHook.isItemDamaged(stack));
    }

    @Inject(method = "getDurabilityForDisplay", at = @At("HEAD"), cancellable = true, remap = false)
    private void sba$getDurabilityForDisplay(ItemStack stack, CallbackInfoReturnable<Double> cir) {
        ReturnValue<Double> returnValue = new ReturnValue<>();
        ItemHook.getDurabilityForDisplay(stack, returnValue);
        if (returnValue.isCancelled()) {
            cir.setReturnValue(returnValue.getReturnValue());
        }
    }
}
