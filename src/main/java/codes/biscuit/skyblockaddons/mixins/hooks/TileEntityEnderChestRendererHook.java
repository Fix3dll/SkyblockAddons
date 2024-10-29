package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnderChestRendererHook {

    private static final ResourceLocation BLANK_ENDERCHEST = new ResourceLocation("skyblockaddons", "blankenderchest.png");

    public static void bindTexture(TileEntityEnderChestRenderer tileEntityEnderChestRenderer, ResourceLocation enderChestTexture) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            tileEntityEnderChestRenderer.bindTexture(BLANK_ENDERCHEST);
        } else {
            tileEntityEnderChestRenderer.bindTexture(enderChestTexture);
        }
    }

    public static void setEnderchestColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            int color = main.getConfigValues().getColor(Feature.MAKE_ENDERCHESTS_GREEN_IN_END);
            if (color == ColorCode.GREEN.getColor()) {
                GlStateManager.color(0, 1, 0); // classic lime green
            } else {
                ColorUtils.bindColor(color);
            }
        }
    }
}
