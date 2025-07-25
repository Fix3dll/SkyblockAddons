package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class IslandButton extends SkyblockAddonsButton {

    @Getter private final ObjectArrayList<IslandMarkerButton> markerButtons = new ObjectArrayList<>();
    @Setter private boolean disableHover = false;

    private long startedHover = -1;
    private long stoppedHover = -1;

    private final IslandWarpGui.Island island;

    private static final int ANIMATION_TIME = 200;

    public IslandButton(IslandWarpGui.Island island) {
        super(island.getX(), island.getY(), Component.literal(island.getLabel()));

        this.island = island;

        for (IslandWarpGui.Marker marker : IslandWarpGui.Marker.values()) {
            if (marker.getIsland() == island) {
                if (marker == IslandWarpGui.Marker.CARNIVAL) {
                    if (!main.getElectionData().isPerkActive("Chivalrous Carnival")) {
                        continue;
                    }
                }
                this.markerButtons.add(new IslandMarkerButton(marker));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawButton(graphics, mouseX, mouseY, true);
    }

    public void drawButton(GuiGraphics graphics, int mouseX, int mouseY, boolean actuallyDraw) {
        Pair<Integer, Integer> scaledMouseLocations = IslandWarpGui.getScaledMouseLocation(mouseX, mouseY);
        mouseX = scaledMouseLocations.getLeft();
        mouseY = scaledMouseLocations.getRight();

        float x = island.getX();
        float y = island.getY();
        float h = island.getH();
        float w = island.getW();

        float centerX = x + (w / 2F);
        float centerY = y + (h / 2F);
        float expansion = 1;
        boolean hovered = false;

        int hoverTime = -1;
        if (isHovering()) {
            hoverTime = (int) (System.currentTimeMillis() - startedHover);
            if (hoverTime > ANIMATION_TIME) {
                hoverTime = ANIMATION_TIME;
            }
        } else if (isStoppingHovering()) {
            hoverTime = (int) (System.currentTimeMillis() - stoppedHover);

            if (hoverTime < ANIMATION_TIME) {
                hoverTime = ANIMATION_TIME - hoverTime;
            } else {
                stoppedHover = -1;
            }
        }

        if (hoverTime != -1) {
            expansion = hoverTime / (float) ANIMATION_TIME * 0.10F + 1;
            h *= expansion;
            w *= expansion;
            x = centerX - (w / 2F);
            y = centerY - (h / 2F);
        }

        if (mouseX > x && mouseY > y && mouseX < x + w && mouseY < y + h) {
            if (island.getNativeImage() != null) {
                int xPixel = Math.round(((mouseX - x) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);
                int yPixel = Math.round(((mouseY - y) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);

                try {
                    int rgb = island.getNativeImage().getPixel(xPixel, yPixel);
                    int alpha = (rgb & 0xFF000000) >> 24;
                    if (alpha != 0) {
                        hovered = true;
                    }
                } catch (IllegalArgumentException ignored) {} // Can't find pixel, it's okay just leave it grey.

            } else {
                hovered = true;
            }
        }

        if (disableHover) {
            disableHover = false;
            hovered = false;
        }

        if (hovered) {
            if (!isHovering()) {
                startedHover = System.currentTimeMillis();

                if (isStoppingHovering()) {
                    int timeSoFar = (int) (System.currentTimeMillis() - stoppedHover);
                    if (timeSoFar > ANIMATION_TIME) {
                        timeSoFar = ANIMATION_TIME;
                    }

                    startedHover -= (ANIMATION_TIME - timeSoFar);
                    stoppedHover = -1;
                }
            }
        } else if (isHovering()) {
            stoppedHover = System.currentTimeMillis();

            int timeSoFar = (int) (System.currentTimeMillis() - startedHover);
            if (timeSoFar > ANIMATION_TIME) {
                timeSoFar = ANIMATION_TIME;
            }

            stoppedHover -= (ANIMATION_TIME - timeSoFar);
            startedHover = -1;
        }

        if (actuallyDraw) {
            int color;
            if (hovered) {
                color = ARGB.white(1F);
            } else {
                color = ARGB.colorFromFloat(1F, 0.9F, 0.9F, 0.9F);
            }

            PoseStack poseStack = graphics.pose();
            final float fX = x, fY = y, fW = w, fH = h;
            graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, island.getResourceLocation(), fX, fY, 0, 0, fW, fH, fW, fH, color));

            for (IslandMarkerButton marker : markerButtons) {
                marker.drawButton(graphics, x, y, expansion, hovered);
            }

            poseStack.pushPose();
            float textScale = 3F * expansion;
            poseStack.scale(textScale, textScale, 1);
            graphics.drawSpecial(source -> MC.font.drawInBatch(
                    getMessage(),
                    centerX / textScale - MC.font.width(getMessage()) / 2F,
                    centerY / textScale,
                    ColorCode.WHITE.getColor(),
                    true,
                    graphics.pose().last().pose(),
                    source,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightTexture.FULL_BRIGHT
            ));
            poseStack.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        int minecraftScale = Minecraft.getMinecraft().gameSettings.guiScale;
//        float islandGuiScale = ISLAND_SCALE;
//
//        mouseX *= minecraftScale;
//        mouseY *= minecraftScale;
//
//        mouseX /= islandGuiScale;
//        mouseY /= islandGuiScale;
//
//        mouseX -= IslandWarpGui.SHIFT_LEFT;
//        mouseY -= IslandWarpGui.SHIFT_TOP;

//        for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
//            System.out.println(island.getLabel()+" "+(mouseX-island.getX()) + " " + (mouseY-island.getY()));
//        }
        return false;
    }

    public boolean isHovering() {
        return startedHover != -1;
    }

    private boolean isStoppingHovering() {
        return stoppedHover != -1;
    }
}
