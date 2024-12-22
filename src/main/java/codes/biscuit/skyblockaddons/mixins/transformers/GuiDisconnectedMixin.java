package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.GuiDisconnectedHook;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public class GuiDisconnectedMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void sba$onDisconnected(GuiScreen guiScreen, String string, IChatComponent iChatComponent, CallbackInfo ci) {
        GuiDisconnectedHook.onDisconnect();
    }
}
