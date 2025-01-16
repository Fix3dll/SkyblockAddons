package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "org.polyfrost.vanillahud.VanillaHUD")
public class VanillaHUDMixin {

    /**
     * @author Fix3dll
     * @reason VanillaHUD legacy SBA compatibility
     */
    @Overwrite(remap = false)
    private static boolean isSBACompactTab() {
        return SkyblockAddons.getInstance().getUtils().isOnSkyblock()
                && Feature.COMPACT_TAB_LIST.isEnabled()
                && TabListParser.getRenderColumns() != null;
    }

}