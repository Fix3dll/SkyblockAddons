package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow private double xpos;
    @Shadow private double ypos;

    @Redirect(method = "grabMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;xpos:D", ordinal = 0, opcode = Opcodes.PUTFIELD))
    public void sba$grabMouse_xpos(MouseHandler instance, double value) {
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isDisabled() || SkyblockAddons.getInstance().getPlayerListener().shouldResetMouse()) {
            this.xpos = value;
        }
    }

    @Redirect(method = "grabMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", ordinal = 0, opcode = Opcodes.PUTFIELD))
    public void sba$grabMouse_ypos(MouseHandler instance, double value) {
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isDisabled() || SkyblockAddons.getInstance().getPlayerListener().shouldResetMouse()) {
            this.ypos = value;
        }
    }

    @Redirect(method = "releaseMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;xpos:D", ordinal = 0, opcode = Opcodes.PUTFIELD))
    public void sba$releaseMouse_xpos(MouseHandler instance, double value) {
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isDisabled() || SkyblockAddons.getInstance().getPlayerListener().shouldResetMouse()) {
            this.ypos = value;
        }
    }

    @Redirect(method = "releaseMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", ordinal = 0, opcode = Opcodes.PUTFIELD))
    public void sba$releaseMouse_ypos(MouseHandler instance, double value) {
        if (Feature.DONT_RESET_CURSOR_INVENTORY.isDisabled() || SkyblockAddons.getInstance().getPlayerListener().shouldResetMouse()) {
            this.ypos = value;
        }
    }

}