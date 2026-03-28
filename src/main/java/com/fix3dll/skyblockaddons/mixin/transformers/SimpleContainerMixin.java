package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleContainer.class)
public class SimpleContainerMixin {

    @Inject(method = "setChanged", at = @At("HEAD"))
    public void sba$setChanged(CallbackInfo ci) {
        SkyblockAddons.getInstance().getScreenListener().containerChanged((SimpleContainer) (Object) this);
    }

}