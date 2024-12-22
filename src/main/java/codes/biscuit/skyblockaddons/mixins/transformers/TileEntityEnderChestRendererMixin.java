package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.TileEntityEnderChestRendererHook;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnderChestRenderer.class)
public abstract class TileEntityEnderChestRendererMixin extends TileEntitySpecialRenderer<TileEntityEnderChest> {

    @Redirect(method = "renderTileEntityAt",
              at = @At(value = "INVOKE",
                      target = "Lnet/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
                      ordinal = 1))
    private void sba$bindTexture(TileEntityEnderChestRenderer instance, ResourceLocation location) {
        TileEntityEnderChestRendererHook.bindTexture(instance, location);
    }

    @Inject(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelChest;renderAll()V", shift = At.Shift.BY, by = -2))
    private void sba$renderAll(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        TileEntityEnderChestRendererHook.setEnderchestColor();
    }
}
