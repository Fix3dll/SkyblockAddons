package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.mixin.accessors.GameOptionsAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class GameOptionsMixin implements GameOptionsAccessor {

    @Mutable
    @Shadow
    @Final
    public KeyMapping[] keyMappings;

    @Unique
    @Override
    public void sba$updateAllKeys(KeyMapping[] updatedAllKeys) {
        keyMappings = updatedAllKeys;
    }

    @Unique
    @Override
    public KeyMapping[] sba$getAllKeys() {
        return this.keyMappings;
    }

    @Inject(method = "load", at = @At("HEAD"))
    public void sba$load(CallbackInfo ci) {
        SkyblockKeyBinding.registerAllKeyBindings((Options) (Object) this);
    }

}