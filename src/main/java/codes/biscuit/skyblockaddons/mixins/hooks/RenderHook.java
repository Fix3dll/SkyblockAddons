package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.EntityOutlines.FeatureTrackerQuest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class RenderHook {

    private static final Minecraft MC = Minecraft.getMinecraft();

    public static void onRenderLivingLabel(Entity entityIn, double x, double y, double z, CallbackInfo ci) {
        if (Feature.TREVOR_BETTER_NAMETAG.isEnabled() && FeatureTrackerQuest.isTrackerConditionsMet()) {

            Entity entityNameTag = MC.theWorld.getEntityByID(FeatureTrackerQuest.getEntityNameTagId());

            // see SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY
            if (entityNameTag != null && entityNameTag.hasCustomName() && entityNameTag == entityIn) {
                Entity renderViewEntity = MC.getRenderViewEntity();

                double distanceScale = Math.max(1, renderViewEntity.getPositionVector().distanceTo(entityNameTag.getPositionVector()) / 5F);

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + 0.5F + getMobHeight(entityNameTag), z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-0.025, -0.025, 0.025);

                GlStateManager.scale(distanceScale, distanceScale, distanceScale);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableTexture2D();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableAlpha();

                MC.fontRendererObj.drawString(
                        entityNameTag.getCustomNameTag(),
                        -MC.fontRendererObj.getStringWidth(entityNameTag.getCustomNameTag()) / 2F,
                        0,
                        -1,
                        true
                );
                ci.cancel();

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    private static double getMobHeight(Entity entityNameTag) {
        List<Entity> surroundingMobs = MC.theWorld.getEntitiesWithinAABB(
                EntityArmorStand.class,
                new AxisAlignedBB(
                        entityNameTag.posX - 0.1,
                        entityNameTag.posY - 3,
                        entityNameTag.posZ - 0.1,
                        entityNameTag.posX + 0.1,
                        entityNameTag.posY,
                        entityNameTag.posZ + 0.1
                )
        );
        if (!surroundingMobs.isEmpty()) {
            return surroundingMobs.get(0).height;
        } else {
            return 1.0F; // default
        }
    }

}