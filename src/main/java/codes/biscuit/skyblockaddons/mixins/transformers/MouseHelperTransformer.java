package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.MouseHelperHook;
import net.minecraft.util.MouseHelper;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MouseHelper.class, remap = false)
public class MouseHelperTransformer {

    @Redirect(method = "ungrabMouseCursor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;setCursorPosition(II)V"))
    private void ungrabMouseCursor(int new_x, int new_y) {
        MouseHelperHook.ungrabMouseCursor(Display.getWidth() / 2, Display.getHeight() / 2);
    }
}
