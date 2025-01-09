package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.features.outline.EntityOutlineRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockPos;

public class RenderGlobalHook {

    public static boolean shouldRenderSkyblockItemOutlines() {
        return EntityOutlineRenderer.shouldRenderEntityOutlines();
    }

    public static void afterFramebufferDraw() {
        GlStateManager.enableDepth();
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.transformer.RenderGlobalTransformer
     */
    @SuppressWarnings("unused")
    public static boolean blockRenderingSkyblockItemOutlines(ICamera camera, float partialTicks, double x, double y, double z) {
        return EntityOutlineRenderer.renderEntityOutlines(camera, partialTicks, x, y, z);
    }

    public static void onAddBlockBreakParticle(int breakerId, BlockPos pos, int progress) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // On public islands, hypixel sends a progress = 10 update once it registers the start of block breaking
        if (breakerId == 0 && main.getUtils().getMap() != Island.PRIVATE_ISLAND &&
                pos.equals(MinecraftHook.prevClickBlock) && progress == 10) {
            //System.out.println(progress);
            MinecraftHook.startMineTime = System.currentTimeMillis();
        }

    }
}
