package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderPlayerHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation CRITICAL = new ResourceLocation("skyblockaddons", "critical.png");
    private static final int CRITICAL_ICON_SIZE = 25;

    public static void onRenderEntityLabel(AbstractClientPlayer player, double x, double y, double z, CallbackInfo ci) {
        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon()
                && (Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled() || Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled())
        ) {
            final Entity renderViewEntity = MC.getRenderViewEntity();
            String profileName = player.getName();
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().getOrDefault(profileName, null);

            if (renderViewEntity != player && dungeonPlayer != null) {
                double newY = y + player.height;

                if (Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled()) {
                    newY += 0.35F;
                }

                if (player.isSneaking()) {
                    newY -= 0.65F;
                }

                double distanceScale = Math.max(1.5, renderViewEntity.getPositionVector().distanceTo(player.getPositionVector()) / 8);

                if (Feature.areEnabled(Feature.ENTITY_OUTLINES, Feature.OUTLINE_DUNGEON_TEAMMATES)) {
                    newY += distanceScale * 0.85F;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, newY, z);
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

                if (Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled() && !dungeonPlayer.isGhost()
                        && (dungeonPlayer.isCritical() || dungeonPlayer.isLow())) {
                    MC.getTextureManager().bindTexture(CRITICAL);
                    DrawUtils.drawModalRectWithCustomSizedTexture(-CRITICAL_ICON_SIZE / 2F, -CRITICAL_ICON_SIZE, 0, 0, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE);

                    String text;
                    if (dungeonPlayer.isLow()) {
                        text = ColorCode.YELLOW + "LOW";
                    } else if (dungeonPlayer.isCritical()) {
                        text = ColorCode.RED + "CRITICAL";
                    } else {
                        text = null;
                    }

                    if (text != null) {
                        MC.fontRendererObj.drawString(
                                text,
                                -MC.fontRendererObj.getStringWidth(text) / 2F,
                                CRITICAL_ICON_SIZE / 2F - 9,
                                -1,
                                true
                        );
                    }
                }

                if (!dungeonPlayer.isGhost() && Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled()) {
                    String nameOverlay =
                            ColorCode.YELLOW + "[" + dungeonPlayer.getDungeonClass().getFirstLetter() + "] "
                                    + ColorCode.GREEN + profileName;
                    MC.fontRendererObj.drawString(
                            nameOverlay,
                            -MC.fontRendererObj.getStringWidth(nameOverlay) / 2F,
                            CRITICAL_ICON_SIZE / 2F + 2,
                            -1,
                            true
                    );

                    String health = dungeonPlayer.getHealth() + (ColorCode.RED + "❤");
                    MC.fontRendererObj.drawString(
                            health,
                            -MC.fontRendererObj.getStringWidth(health) / 2F,
                            CRITICAL_ICON_SIZE / 2F + 13,
                            -1,
                            true
                    );
                    ci.cancel();
                }

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }
}