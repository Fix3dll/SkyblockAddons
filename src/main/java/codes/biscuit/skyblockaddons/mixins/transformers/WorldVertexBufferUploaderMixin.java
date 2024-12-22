package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.WorldVertexBufferUploaderHook;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldVertexBufferUploader.class)
public class WorldVertexBufferUploaderMixin {

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void sba$draw(WorldRenderer p_181679_1_, CallbackInfo ci) {
        if (WorldVertexBufferUploaderHook.onRenderWorldRendererBuffer()) {
            ci.cancel();
        }
    }
}
