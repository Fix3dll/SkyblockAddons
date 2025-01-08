package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.EntityPlayerSPHook;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    public void sba$dropOneItem(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        EntityPlayerSPHook.dropOneItemConfirmation(cir);
    }
}
