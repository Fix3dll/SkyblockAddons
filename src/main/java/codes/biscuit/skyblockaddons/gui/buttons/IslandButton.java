package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.ElectionData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class IslandButton extends GuiButton {

    @Getter private final List<IslandMarkerButton> markerButtons = new ArrayList<>();

    @Setter
    private boolean disableHover = false;

    private long startedHover = -1;
    private long stoppedHover = -1;

    private final IslandWarpGui.Island island;

    private static final int ANIMATION_TIME = 200;

    public IslandButton(IslandWarpGui.Island island) {
        super(0, island.getX(), island.getY(), island.getLabel());

        this.island = island;

        for (IslandWarpGui.Marker marker : IslandWarpGui.Marker.values()) {
            if (marker.getIsland() == island) {
                if (marker == IslandWarpGui.Marker.CARNIVAL) {
                    ElectionData electionData = SkyblockAddons.getInstance().getElectionData();
                    if (electionData != null && !electionData.isPerkActive("Chivalrous Carnival")) {
                        continue;
                    }
                }
                this.markerButtons.add(new IslandMarkerButton(marker));
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        drawButton(mc, mouseX, mouseY, true);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean actuallyDraw) {
        Pair<Integer, Integer> scaledMouseLocations = IslandWarpGui.getScaledMouseLocation(mouseX, mouseY);
        mouseX = scaledMouseLocations.getKey();
        mouseY = scaledMouseLocations.getValue();

        float x = island.getX();
        float y = island.getY();
        float h = island.getH();
        float w = island.getW();

        float centerX = x+(w/2F);
        float centerY = y+(h/2F);
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

        if (mouseX > x && mouseY > y && mouseX < x+w && mouseY < y+h) {
            if (island.getBufferedImage() != null) {
                int xPixel = Math.round(((mouseX - x) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);
                int yPixel = Math.round(((mouseY - y) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);

                try {
                    int rgb = island.getBufferedImage().getRGB(xPixel, yPixel);
                    int alpha = (rgb & 0xff000000) >> 24;
                    if (alpha != 0) {
                        hovered = true;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    // Can't find pixel, its okay just leave it grey.
                }
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
                    int timeSoFar = (int)(System.currentTimeMillis()-stoppedHover);
                    if (timeSoFar > ANIMATION_TIME) {
                        timeSoFar = ANIMATION_TIME;
                    }

                    startedHover -= (ANIMATION_TIME-timeSoFar);
                    stoppedHover = -1;
                }
            }
        } else if (isHovering()) {
            stoppedHover = System.currentTimeMillis();

            int timeSoFar = (int)(System.currentTimeMillis()-startedHover);
            if (timeSoFar > ANIMATION_TIME) {
                timeSoFar = ANIMATION_TIME;
            }

            stoppedHover -= (ANIMATION_TIME-timeSoFar);
            startedHover = -1;
        }

        if (actuallyDraw) {
            if (hovered) {
                GlStateManager.color(1F, 1F, 1F, 1F);
            } else {
                GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
            }

            mc.getTextureManager().bindTexture(island.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);

            for (IslandMarkerButton marker : markerButtons) {
                marker.drawButton(x, y, expansion, hovered);
            }

            GlStateManager.pushMatrix();
            float textScale = 3F;
            textScale *= expansion;
            GlStateManager.scale(textScale, textScale, 1);

            mc.fontRendererObj.drawStringWithShadow(
                    displayString,
                    centerX / textScale - mc.fontRendererObj.getStringWidth(displayString) / 2F,
                    centerY / textScale,
                    ColorCode.WHITE.getColor()
            );

            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
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
