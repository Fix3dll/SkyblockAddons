package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.EntityLivingBaseHook;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {

    // Objective: (not implemented because of removed feature)
    // Find: this.hurtTime =
    // Insert After: EntityLivingBaseHook.onResetHurtTime();

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "removePotionEffectClient", at = @At("HEAD"), cancellable = true)
    private void sba$removePotionEffectClient(int potionId, CallbackInfo ci) {
        if (EntityLivingBaseHook.onRemovePotionEffect((EntityLivingBase) (Object) this, potionId)) {
            ci.cancel();
        }
    }

    @Inject(method = "addPotionEffect", at = @At("HEAD"))
    private void sba$addPotionEffect(PotionEffect potioneffectIn, CallbackInfo ci) {
        EntityLivingBaseHook.onAddPotionEffect((EntityLivingBase) (Object) this, potioneffectIn);
    }
}
