package com.fix3dll.skyblockaddons.mixin.transformers;

import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SimpleContainer.class)
public class SimpleContainerMixin {

    @Shadow private @Nullable List<ContainerListener> listeners;

    // FIXME is it really needed?
    @Inject(method = "setChanged", at = @At("HEAD"), cancellable = true)
    public void sba$setChanged(CallbackInfo ci) {
        if (this.listeners != null) {
            List<ContainerListener> copyList = new ArrayList<>(listeners);
            for (ContainerListener listener : copyList) {
                listener.containerChanged((SimpleContainer) (Object) this);
            }
            ci.cancel();
        }
    }

}