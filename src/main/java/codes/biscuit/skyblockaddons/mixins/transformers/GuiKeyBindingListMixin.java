package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.SkyblockKeyBinding;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(GuiKeyBindingList.KeyEntry.class)
public class GuiKeyBindingListMixin {

    @Redirect(method = "<init>(Lnet/minecraft/client/gui/GuiKeyBindingList;Lnet/minecraft/client/settings/KeyBinding;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", ordinal = 0))
    private String sba$translateKeyDesc(String descKey, Object[] parameters) {
        // Fixes SBA keybinding translation issues
        if (descKey.contains("skyblockaddons")) {
            for (SkyblockKeyBinding skb : SkyblockKeyBinding.values()) {
                if (Objects.equals(skb.getKeyBinding().keyDescription, descKey)) {
                    return Translations.getMessage(skb.getTranslationKey());
                }
            }
        }
        return I18n.format(descKey);
    }

}
