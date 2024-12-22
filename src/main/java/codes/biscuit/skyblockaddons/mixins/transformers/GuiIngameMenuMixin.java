package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.GuiDisconnectedHook;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiIngameMenuHook;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class GuiIngameMenuMixin extends GuiScreen {

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void sba$onButtonClick(GuiButton button, CallbackInfo ci) {
        if (button.id == 53) {
            GuiIngameMenuHook.onButtonClick();
        }
    }

    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z", shift = At.Shift.BY, by = -2))
    private void sba$onDisconnect(GuiButton button, CallbackInfo ci) {
        GuiDisconnectedHook.onDisconnect();
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void sba$addMenuButtons(CallbackInfo ci) {
        GuiIngameMenuHook.addMenuButtons(this.buttonList, this.width, this.height);
    }
}
