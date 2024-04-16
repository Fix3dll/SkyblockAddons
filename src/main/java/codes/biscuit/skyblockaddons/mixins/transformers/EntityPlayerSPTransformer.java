package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.EntityPlayerSPHook;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPTransformer {

    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    public void dropOneItem(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        ReturnValue<EntityItem> returnValue = new ReturnValue<>();
        EntityPlayerSPHook.dropOneItemConfirmation(returnValue);

        if (returnValue.isCancelled()) {
            cir.setReturnValue(returnValue.getReturnValue());
        }
    }
}
