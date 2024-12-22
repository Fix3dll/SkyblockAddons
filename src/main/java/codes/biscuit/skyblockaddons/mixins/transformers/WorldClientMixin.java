package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.WorldClientHook;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public class WorldClientMixin {

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    private void sba$onEntityRemoved(Entity entityIn, CallbackInfo ci) {
        WorldClientHook.onEntityRemoved(entityIn);
    }

    @Inject(method = "invalidateRegionAndSetBlock", at = @At("HEAD"))
    private void sba$invalidateRegionAndSetBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        WorldClientHook.blockUpdated(pos, state);
    }
}
