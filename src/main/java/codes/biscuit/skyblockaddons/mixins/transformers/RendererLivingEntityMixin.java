package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.RendererLivingEntityHook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class RendererLivingEntityMixin<T extends EntityLivingBase> {

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean sba$rotateCorpse(String displayName, Object v2, T bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        return RendererLivingEntityHook.isCoolPerson(displayName);
    }

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z"))
    private boolean sba$rotateCorpse(EntityPlayer bat, EnumPlayerModelParts p_175148_1_) {
        return RendererLivingEntityHook.isWearing(bat, EnumPlayerModelParts.CAPE);
    }

    @Redirect(method = "setScoreTeamColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getColorCode(C)I"))
    private int sba$setScoreTeamColor(FontRenderer instance, char character, T entityLivingBaseIn) {
        return RendererLivingEntityHook.setOutlineColor(entityLivingBaseIn, 16777215);
    }
}
