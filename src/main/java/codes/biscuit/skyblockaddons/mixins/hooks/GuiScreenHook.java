package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public class GuiScreenHook {

    public static boolean onRenderTooltip(ItemStack itemStack, int x, int y) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) && main.getUtils().isEmptyGlassPane(itemStack)) {
            return true;
        }

        if (main.getConfigValues().isDisabled(Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS) && (main.getInventoryUtils().getInventoryType() == InventoryType.ULTRASEQUENCER || main.getInventoryUtils().getInventoryType() == InventoryType.CHRONOMATRON)) {
            return true;
        }

        return ContainerPreviewManager.onRenderTooltip(itemStack, x, y);
    }

    //TODO: Fix for Hypixel localization
    public static void handleComponentClick(IChatComponent component) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        /* Deprecated example
        if (main.getUtils().isOnSkyblock() && component != null
                // The prompt when Maddox picks up the phone.
                && "§2§l[OPEN MENU]".equals(component.getUnformattedText())
                && !CooldownManager.isOnCooldown("AATROX_BATPHONE") {
            CooldownManager.put("AATROX_BATPHONE");
        }
        */
    }
}
