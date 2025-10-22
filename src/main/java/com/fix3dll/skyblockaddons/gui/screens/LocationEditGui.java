package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureGuiData;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.render.state.FillAbsoluteRenderState;
import com.fix3dll.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonColorWheel;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonLocation;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonResize;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSolid;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.DrawType;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LocationEditGui extends SkyblockAddonsScreen {

    private static final int BOX_HEIGHT = 20;
    private static final int SNAPPING_RADIUS = 120;
    private static final int SNAP_PULL = 1;

    @Getter private static EditMode editMode = EditMode.RESCALE_FEATURES;

    /**
     * The feature that is currently being dragged, or null for nothing.
     */
    private Feature draggedFeature;
    @Getter private static boolean resizing;
    private ButtonResize.Corner resizingCorner;

    private float xOffset;
    private float yOffset;

    @Getter private final int lastPage;
    @Getter private final EnumUtils.GuiTab lastTab;

    private final Map<Feature, ButtonLocation> buttonLocations = new EnumMap<>(Feature.class);

    @Setter private boolean closing = false;
    private static boolean tipShown = false;
    private GuiGraphics guiGraphics;
    private boolean isMiddlePressed = false;

    public LocationEditGui(int lastPage, EnumUtils.GuiTab lastTab) {
        super(Component.empty());
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @Override
    public void init() {
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            // Don't display features that have been disabled
            if (feature.isGuiFeature() && feature.isEnabled()) {
                ButtonLocation buttonLocation = new ButtonLocation(feature);
                addRenderableWidget(buttonLocation);
                buttonLocations.put(feature, buttonLocation);
            }
        }

        if (editMode != EditMode.NONE) {
            addResizeButtons();
        }
        if (Feature.SHOW_COLOR_ICONS.isEnabled()) {
            addColorWheelsToAllFeatures();
        }

        Window window = MC.getWindow();
        Set<Feature> guiFeatures = Feature.getEditGuiFeatures();
        guiFeatures.removeIf(Feature::isRemoteDisabled);
        int numButtons = guiFeatures.size();
        int x;
        int y = window.getGuiScaledHeight() / 2;
        // List may change later
        //noinspection ConstantConditions
        if (numButtons % 2 == 0) {
            y -= Math.round((numButtons / 2F) * (BOX_HEIGHT + 5)) - 5;
        } else {
            y -= Math.round(((numButtons - 1) / 2F) * (BOX_HEIGHT + 5)) + 20;
        }

        for (Feature feature : guiFeatures) {
            String featureName = feature.getMessage();
            int boxWidth = feature == Feature.RESCALE_FEATURES ? SkyblockAddonsGui.BUTTON_MAX_WIDTH : MC.font.width(featureName) + 10;
            if (boxWidth > SkyblockAddonsGui.BUTTON_MAX_WIDTH) boxWidth = SkyblockAddonsGui.BUTTON_MAX_WIDTH;
            x = window.getGuiScaledWidth() / 2 - boxWidth / 2;
            y += BOX_HEIGHT + 5;

            if (feature == Feature.RESCALE_FEATURES) {
                addRenderableWidget(new ButtonCycling(x, y, boxWidth, BOX_HEIGHT, Arrays.asList(EditMode.values()), editMode.ordinal(), index -> {
                    editMode = EditMode.values()[index];
                    closing = true;
                    MC.setScreen(new LocationEditGui(lastPage, lastTab));
                    closing = false;
                    addResizeButtons();
                }));
            } else if (feature == Feature.RESET_LOCATION) {
                addRenderableWidget(new ButtonSolid(x, y, boxWidth, BOX_HEIGHT, featureName, feature, 0xFF7878, false));
            } else {
                addRenderableWidget(new ButtonSolid(x, y, boxWidth, BOX_HEIGHT, featureName, feature, true));
            }
        }

        // 1 tip per session :)
        if (MC.player != null && !tipShown) {
            tipShown = true;
            Utils.sendMessage(ColorCode.GREEN + Translations.getMessage("messages.locationEditGui.atOpening"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.guiGraphics = graphics;
        Snap[] snaps = checkSnapping();

        onMouseMove(mouseX, mouseY, snaps);

        if (editMode != EditMode.NONE) {
            recalculateResizeButtons();
        }
        if (Feature.SHOW_COLOR_ICONS.isEnabled()) {
            recalculateColorWheels();
        }
        drawGradientBackground(graphics, 64, 128);

        Feature lastHoveredFeature = ButtonLocation.getLastHoveredFeature();

        for (EnumUtils.AnchorPoint anchorPoint : EnumUtils.AnchorPoint.values()) {
            int x = anchorPoint.getX(MC.getWindow().getGuiScaledWidth());
            int y = anchorPoint.getY(MC.getWindow().getGuiScaledHeight());
            int color = lastHoveredFeature != null && lastHoveredFeature.getAnchorPoint() == anchorPoint
                    ? ColorCode.RED.getColor(127)
                    : ColorCode.YELLOW.getColor(127);
            graphics.guiRenderState.submitGuiElement(
                    new FillAbsoluteRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), graphics.pose(), x -4, y - 4, x + 4, y + 4, color, graphics.scissorStack.peek())
            );
        }
        super.render(graphics, mouseX, mouseY, partialTick); // Draw buttons.

        if (snaps != null) {
            for (Snap snap : snaps) {
                if (snap != null) {
                    float left = snap.getRectangle().get(Edge.LEFT);
                    float top = snap.getRectangle().get(Edge.TOP);
                    float right = snap.getRectangle().get(Edge.RIGHT);
                    float bottom = snap.getRectangle().get(Edge.BOTTOM);

                    if (snap.getWidth() < 0.5) {
                        float averageX = (left + right) / 2;
                        left = averageX - 0.25F;
                        right = averageX + 0.25F;
                    }
                    if (snap.getHeight() < 0.5) {
                        float averageY = (top + bottom) / 2;
                        top = averageY - 0.25F;
                        bottom = averageY + 0.25F;
                    }

                    final int color;
                    if ((right - left) == 0.5 || (bottom - top) == 0.5) {
                        color = 0xFF00FF00;
                    } else {
                        color = 0xFFFF0000;
                    }
                    graphics.guiRenderState.submitGuiElement(
                            new FillAbsoluteRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), graphics.pose(), left, top, right, bottom, color, graphics.scissorStack.peek())
                    );
                }
            }
        }

        ButtonLocation lastHoveredButton = resizing ? buttonLocations.get(draggedFeature) : getHoveredFeatureButton(mouseX, mouseY);
//        System.out.printf("resizing: %s, lastHoveredButton: %s%n", resizing, lastHoveredButton);

        // Draw location information of hovered feature to the middle of screen
        drawFeatureCoords(graphics, lastHoveredButton);

        if (lastHoveredButton != null && this.isMiddlePressed) {
            closing = true;
            MC.setScreen(new SettingsGui(lastHoveredButton.feature, 1, lastPage, lastTab, EnumUtils.GUIType.EDIT_LOCATIONS));
        }
    }

    private void addResizeButtons() {
        clearAllResizeButtons();
        switch (editMode) {
            case RESIZE_BARS -> {
                // Add all gui elements that can be edited to the gui.
                for (Feature feature : Feature.getGuiFeatures()) {
                    // Don't display features that have been disabled
                    if (feature.isEnabled()) {
                        FeatureGuiData guiFeatureData = feature.getFeatureGuiData();
                        if (guiFeatureData != null && guiFeatureData.getDrawType() == DrawType.BAR) {
                            addResizeCorners(feature);
                        }
                    }
                }
            }
            case RESCALE_FEATURES -> {
                // Add all gui elements that can be edited to the gui.
                for (Feature feature : Feature.getGuiFeatures()) {
                    if (feature.isEnabled()) { // Don't display features that have been disabled
                        addResizeCorners(feature);
                    }
                }
            }
            case NONE -> {
                // ignored
            }
        }
    }

    private void clearAllResizeButtons() {
        children().removeIf(guiEventListener -> guiEventListener instanceof ButtonResize);
        renderables.removeIf(renderable -> renderable instanceof ButtonResize);
    }

    private void clearAllColorWheelButtons() {
        children().removeIf(guiEventListener -> guiEventListener instanceof ButtonColorWheel);
        renderables.removeIf(renderable -> renderable instanceof ButtonColorWheel);
    }

    private void addColorWheelsToAllFeatures() {
        for (ButtonLocation buttonLocation : buttonLocations.values()) {
            Feature feature = buttonLocation.getFeature();

            if (feature.getFeatureGuiData() == null || feature.getFeatureGuiData().getDefaultColor() == null) {
                continue;
            }

            float scaleX = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
            float scaleY = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ? feature.getFeatureData().getSizesY() : 1;
            float boxXOne = buttonLocation.getBoxXOne() * scaleX;
            float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
            float boxYOne = buttonLocation.getBoxYOne() * scaleY;
            float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;
            float y = boxYOne + (boxYTwo - boxYOne) / 2F - ButtonColorWheel.SIZE / 2F;
            float x;

            if (feature.getAnchorPoint().isOnLeft()) {
                x = boxXTwo + 2;
            } else {
                x = boxXOne - ButtonColorWheel.SIZE - 2;
            }

            addRenderableWidget(new ButtonColorWheel(Math.round(x), Math.round(y), feature));
        }
    }

    private void addResizeCorners(Feature feature) {
        children().removeIf(guiEventListener -> guiEventListener instanceof ButtonResize buttonResize && buttonResize.getFeature() == feature);
        renderables.removeIf(renderable -> renderable instanceof ButtonResize buttonResize && buttonResize.getFeature() == feature);

        ButtonLocation buttonLocation = buttonLocations.get(feature);
        if (buttonLocation == null) {
            return;
        }

        float scale = buttonLocation.getScale();
        float scaledX1 = buttonLocation.getBoxXOne() * scale * buttonLocation.getScaleX();
        float scaledY1 = buttonLocation.getBoxYOne() * scale * buttonLocation.getScaleY();
        float scaledX2 = buttonLocation.getBoxXTwo() * scale * buttonLocation.getScaleX();
        float scaledY2 = buttonLocation.getBoxYTwo() * scale * buttonLocation.getScaleY();
        addRenderableWidget(new ButtonResize(scaledX1, scaledY1, feature, ButtonResize.Corner.TOP_LEFT));
        addRenderableWidget(new ButtonResize(scaledX2, scaledY1, feature, ButtonResize.Corner.TOP_RIGHT));
        addRenderableWidget(new ButtonResize(scaledX1, scaledY2, feature, ButtonResize.Corner.BOTTOM_LEFT));
        addRenderableWidget(new ButtonResize(scaledX2, scaledY2, feature, ButtonResize.Corner.BOTTOM_RIGHT));
    }

    /**
     * @return {@code ButtonLocation} the mouse is currently hovering over or {@code null} if the mouse is not hovering
     * over any
     */
    private ButtonLocation getHoveredFeatureButton(double mouseX, double mouseY) {
        ButtonLocation lastHovered = null;
        for (ButtonLocation buttonLocation : buttonLocations.values()) {
            if (buttonLocation.isMouseOver(mouseX, mouseY)) {
                lastHovered = buttonLocation;
            }
        }
        return lastHovered;
    }

    private void recalculateResizeButtons() {
        renderables.forEach(renderable -> {
            if (renderable instanceof ButtonResize buttonResize) {
                ButtonResize.Corner corner = buttonResize.getCorner();
                Feature feature = buttonResize.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    return;
                }

                float scaleX = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
                float scaleY = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ?feature.getFeatureData().getSizesY() : 1;
                float boxXOne = buttonLocation.getBoxXOne() * scaleX;
                float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
                float boxYOne = buttonLocation.getBoxYOne() * scaleY;
                float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;

                if (corner == ButtonResize.Corner.TOP_LEFT) {
                    buttonResize.resizeX = boxXOne;
                    buttonResize.resizeY = boxYOne;
                } else if (corner == ButtonResize.Corner.TOP_RIGHT) {
                    buttonResize.resizeX = boxXTwo;
                    buttonResize.resizeY = boxYOne;
                } else if (corner == ButtonResize.Corner.BOTTOM_LEFT) {
                    buttonResize.resizeX = boxXOne;
                    buttonResize.resizeY = boxYTwo;
                } else if (corner == ButtonResize.Corner.BOTTOM_RIGHT) {
                    buttonResize.resizeX = boxXTwo;
                    buttonResize.resizeY = boxYTwo;
                }
            }
        });
    }

    private void recalculateColorWheels() {
        renderables.forEach(renderable -> {
            if (renderable instanceof ButtonColorWheel buttonColorWheel) {
                Feature feature = buttonColorWheel.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    return;
                }

                float scaleX = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
                float scaleY = feature.getFeatureGuiData().getDrawType() == DrawType.BAR ? feature.getFeatureData().getSizesY() : 1;
                float boxXOne = buttonLocation.getBoxXOne() * scaleX;
                float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
                float boxYOne = buttonLocation.getBoxYOne() * scaleY;
                float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;
                float y = boxYOne + (boxYTwo - boxYOne) / 2F - ButtonColorWheel.SIZE / 2F;
                float x;

                if (feature.getAnchorPoint().isOnLeft()) {
                    x = boxXTwo + 2;
                } else {
                    x = boxXOne - ButtonColorWheel.SIZE - 2;
                }

                buttonColorWheel.colorWheelX = x;
                buttonColorWheel.colorWheelY = y;
            }
        });
    }

    public Snap[] checkSnapping() {
        if (Feature.ENABLE_FEATURE_SNAPPING.isDisabled()) return null;

        if (draggedFeature != null) {
            ButtonLocation thisButton = buttonLocations.get(draggedFeature);
            if (thisButton == null) {
                return null;
            }

            Snap horizontalSnap = null;
            Snap verticalSnap = null;

            for (Map.Entry<Feature, ButtonLocation> buttonLocationEntry : this.buttonLocations.entrySet()) {
                ButtonLocation otherButton = buttonLocationEntry.getValue();

                if (otherButton == thisButton) continue;

                for (Edge otherEdge : Edge.getHorizontalEdges()) {
                    for (Edge thisEdge : Edge.getHorizontalEdges()) {

                        float deltaX = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton);

                        if (Math.abs(deltaX) <= SNAP_PULL) {
                            float deltaY = Edge.TOP.getCoordinate(otherButton) - Edge.TOP.getCoordinate(thisButton);

                            float topY;
                            float bottomY;
                            if (deltaY > 0) {
                                topY = Edge.BOTTOM.getCoordinate(thisButton);
                                bottomY = Edge.TOP.getCoordinate(otherButton);
                            } else {
                                topY = Edge.BOTTOM.getCoordinate(otherButton);
                                bottomY = Edge.TOP.getCoordinate(thisButton);
                            }

                            float snapX = otherEdge.getCoordinate(otherButton);
                            Snap thisSnap = new Snap(otherEdge.getCoordinate(otherButton), topY, thisEdge.getCoordinate(thisButton), bottomY, thisEdge, otherEdge, snapX);

                            if (thisSnap.getHeight() < SNAPPING_RADIUS) {
                                if (horizontalSnap == null || thisSnap.getHeight() < horizontalSnap.getHeight()) {
                                    if (Feature.DEVELOPER_MODE.isEnabled() && guiGraphics != null) {
                                        guiGraphics.guiRenderState.submitGuiElement(
                                                new FillAbsoluteRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), guiGraphics.pose(), snapX - 0.5F, 0, snapX + 0.5F, MC.getWindow().getHeight(), 0xFF0000FF, guiGraphics.scissorStack.peek())
                                        );
                                    }
                                    horizontalSnap = thisSnap;
                                }
                            }
                        }
                    }
                }

                for (Edge otherEdge : Edge.getVerticalEdges()) {
                    for (Edge thisEdge : Edge.getVerticalEdges()) {

                        float deltaY = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton);

                        if (Math.abs(deltaY) <= SNAP_PULL) {
                            float deltaX = Edge.LEFT.getCoordinate(otherButton) - Edge.LEFT.getCoordinate(thisButton);

                            float leftX;
                            float rightX;
                            if (deltaX > 0) {
                                leftX = Edge.RIGHT.getCoordinate(thisButton);
                                rightX = Edge.LEFT.getCoordinate(otherButton);
                            } else {
                                leftX = Edge.RIGHT.getCoordinate(otherButton);
                                rightX = Edge.LEFT.getCoordinate(thisButton);
                            }
                            float snapY = otherEdge.getCoordinate(otherButton);
                            Snap thisSnap = new Snap(leftX, otherEdge.getCoordinate(otherButton), rightX, thisEdge.getCoordinate(thisButton), thisEdge, otherEdge, snapY);

                            if (thisSnap.getWidth() < SNAPPING_RADIUS) {
                                if (verticalSnap == null || thisSnap.getWidth() < verticalSnap.getWidth()) {
                                    if (Feature.DEVELOPER_MODE.isEnabled() && guiGraphics != null) {
                                        guiGraphics.guiRenderState.submitGuiElement(
                                                new FillAbsoluteRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), guiGraphics.pose(), 0, snapY - 0.5F, MC.getWindow().getWidth(), snapY + 0.5F, 0xFF0000FF, guiGraphics.scissorStack.peek())
                                        );
                                    }
                                    verticalSnap = thisSnap;
                                }
                            }
                        }
                    }
                }
            }

            return new Snap[]{horizontalSnap, verticalSnap};
        }

        return null;
    }

    public enum Edge {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,

        HORIZONTAL_MIDDLE,
        VERTICAL_MIDDLE,
        ;

        @Getter
        private static final Set<Edge> verticalEdges = Sets.newHashSet(TOP, BOTTOM, HORIZONTAL_MIDDLE);
        @Getter
        private static final Set<Edge> horizontalEdges = Sets.newHashSet(LEFT, RIGHT, VERTICAL_MIDDLE);

        public float getCoordinate(ButtonLocation button) {
            return switch (this) {
                case LEFT -> button.getBoxXOne() * button.getScale() * button.getScaleX();
                case TOP -> button.getBoxYOne() * button.getScale() * button.getScaleY();
                case RIGHT -> button.getBoxXTwo() * button.getScale() * button.getScaleX();
                case BOTTOM -> button.getBoxYTwo() * button.getScale() * button.getScaleY();
                case HORIZONTAL_MIDDLE ->
                        TOP.getCoordinate(button) + (BOTTOM.getCoordinate(button) - TOP.getCoordinate(button)) / 2F;
                case VERTICAL_MIDDLE ->
                        LEFT.getCoordinate(button) + (RIGHT.getCoordinate(button) - LEFT.getCoordinate(button)) / 2F;
                default -> 0;
            };
        }
    }

//    @Override
//    public void mouseMoved(double mouseX, double mouseY) {
//        onMouseMove((int) mouseX, (int) mouseY, checkSnapping());
//        System.out.println("resizing: " + resizing + ", draggedFeature: " + (draggedFeature == null ? "null" : draggedFeature.name()));
//    }

    /**
     * Set the coordinates when the mouse moves.
     */
    protected void onMouseMove(int mouseX, int mouseY, Snap[] snaps) {
        ButtonLocation buttonLocation = buttonLocations.get(draggedFeature);
        if (buttonLocation == null) {
            return;
        }

        float floatMouseX = (float) MC.mouseHandler.getScaledXPos(MC.getWindow());
        float floatMouseY = (float) MC.mouseHandler.getScaledYPos(MC.getWindow());

        float scale = buttonLocation.getScale();
        float scaledX1 = buttonLocation.getBoxXOne() * scale * buttonLocation.getScaleX();
        float scaledY1 = buttonLocation.getBoxYOne() * scale * buttonLocation.getScaleY();
        float scaledX2 = buttonLocation.getBoxXTwo() * scale * buttonLocation.getScaleX();
        float scaledY2 = buttonLocation.getBoxYTwo() * scale * buttonLocation.getScaleY();
        float scaledWidth = scaledX2 - scaledX1;
        float scaledHeight = scaledY2 - scaledY1;

        if (resizing) {
            float scaledMiddleX = (scaledX1 + scaledX2) / 2;
            float scaledMiddleY = (scaledY1 + scaledY2) / 2;

            if (editMode == EditMode.RESIZE_BARS) {
                float scaleX = (floatMouseX - scaledMiddleX) / (xOffset - scaledMiddleX);
                float scaleY = (floatMouseY - scaledMiddleY) / (yOffset - scaledMiddleY);
                scaleX = Math.max(Math.min(scaleX, 5F), .25F);
                scaleY = Math.max(Math.min(scaleY, 5F), .25F);

                draggedFeature.getFeatureData().getBarSizes().setLeft(scaleX);
                draggedFeature.getFeatureData().getBarSizes().setRight(scaleY);

                if (this.guiGraphics != null) {
                    buttonLocation.renderWidget(guiGraphics, mouseX, mouseY, 0);
                }

            } else if (editMode == EditMode.RESCALE_FEATURES) {

                float xOffset = floatMouseX - this.xOffset * scale * buttonLocation.getScaleX() - scaledMiddleX;
                float yOffset = floatMouseY - this.yOffset * scale * buttonLocation.getScaleY() - scaledMiddleY;

                if (resizingCorner == ButtonResize.Corner.TOP_LEFT) {
                    xOffset *= -1;
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.TOP_RIGHT) {
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.BOTTOM_LEFT) {
                    xOffset *= -1;
                }

                float width = (buttonLocation.getBoxXTwo() - buttonLocation.getBoxXOne());
                float height = (buttonLocation.getBoxYTwo() - buttonLocation.getBoxYOne());
                float newWidth = xOffset * 2F;
                float newHeight = yOffset * 2F;

                float scaleX = newWidth / width;
                float scaleY = newHeight / height;

                float oldScale = draggedFeature.getGuiScale();
                float newScale = Math.max(scaleX, scaleY);

                if (Math.abs(newScale - oldScale) > 0.01F) {
                    draggedFeature.setGuiScale(newScale);
                    if (this.guiGraphics != null) {
                        buttonLocation.renderWidget(guiGraphics, mouseX, mouseY, 0);
                    }
                }
            }
        } else if (draggedFeature != null) {
            Snap horizontalSnap = null;
            Snap verticalSnap = null;
            if (snaps != null) {
                horizontalSnap = snaps[0];
                verticalSnap = snaps[1];
            }

            Window window = MC.getWindow();

            float x = floatMouseX - draggedFeature.getAnchorPoint().getX(window.getGuiScaledWidth());
            float y = floatMouseY - draggedFeature.getAnchorPoint().getY(window.getGuiScaledHeight());

            boolean xSnapped = false;
            boolean ySnapped = false;

            if (horizontalSnap != null) {
                float snapX = horizontalSnap.getSnapValue();

                if (horizontalSnap.getThisSnapEdge() == Edge.LEFT) {
                    float snapOffset = Math.abs((floatMouseX - this.xOffset) - (snapX + scaledWidth / 2F));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(window.getGuiScaledWidth()) + scaledWidth / 2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.RIGHT) {
                    float snapOffset = Math.abs((floatMouseX - this.xOffset) - (snapX - scaledWidth / 2F));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(window.getGuiScaledWidth()) - scaledWidth / 2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.VERTICAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseX - this.xOffset) - (snapX));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(window.getGuiScaledWidth());
                    }
                }
            }

            if (verticalSnap != null) {
                float snapY = verticalSnap.getSnapValue();

                if (verticalSnap.getThisSnapEdge() == Edge.TOP) {
                    float snapOffset = Math.abs((floatMouseY - this.yOffset) - (snapY + scaledHeight / 2F));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(window.getGuiScaledHeight()) + scaledHeight / 2F;
                    }

                } else if (verticalSnap.getThisSnapEdge() == Edge.BOTTOM) {
                    float snapOffset = Math.abs((floatMouseY - this.yOffset) - (snapY - scaledHeight / 2F));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(window.getGuiScaledHeight()) - scaledHeight / 2F;
                    }
                } else if (verticalSnap.getThisSnapEdge() == Edge.HORIZONTAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseY - this.yOffset) - (snapY));
                    if (snapOffset <= SNAP_PULL * window.getGuiScale()) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(window.getGuiScaledHeight());
                    }
                }
            }

            if (!xSnapped) {
                x -= xOffset;
            }

            if (!ySnapped) {
                y -= yOffset;
            }

            if (xSnapped || ySnapped) {
                Pair<Float, Float> relativeCoords = draggedFeature.getRelativeCoords();
                float xChange = Math.abs(relativeCoords.getLeft() - x);
                float yChange = Math.abs(relativeCoords.getRight() - y);
                if (xChange < 0.001 && yChange < 0.001) {
                    return;
                }
            }

            draggedFeature.getFeatureData().setCoords(x, y);
            draggedFeature.setClosestAnchorPoint();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Rescaling features with mouse buttons
        ButtonLocation lastHoveredButton = resizing ? buttonLocations.get(draggedFeature) : getHoveredFeatureButton(mouseX, mouseY);
        rescaleWithScroll(lastHoveredButton, scrollX, scrollY);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void rescaleWithScroll(ButtonLocation lastHoveredButton, double scrollX, double scrollY) {
        if (lastHoveredButton == null || editMode != EditMode.RESCALE_FEATURES) return;

        Feature lastHoveredFeature = lastHoveredButton.getFeature();
        if (lastHoveredFeature == null) return;

        float oldScale = lastHoveredFeature.getGuiScale();

        // Rescale feature with mouse scroll
        if (scrollX > 0 || scrollY > 0) {
            float newScale = oldScale + (InputConstants.isKeyDown(MC.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 1.0F : 0.1F);
            lastHoveredFeature.setGuiScale(newScale);
            recalculateResizeButtons();
        } else if (scrollX < 0 || scrollY < 0) {
            float newScale = oldScale - (InputConstants.isKeyDown(MC.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 1.0F : 0.1F);
            lastHoveredFeature.setGuiScale(newScale);
            recalculateResizeButtons();
        }
    }

    private void drawFeatureCoords(GuiGraphics graphics, ButtonLocation lastHoveredButton) {
        if (editMode != EditMode.NONE) {
            final Window window = MC.getWindow();
            final double x = window.getGuiScaledWidth() / 2D;
            final double y = window.getGuiScaledHeight() / 2D;
            final int boxCount = (int) Math.ceil(Feature.getEditGuiFeatures().size() / 2D);

            // mouseX/Y for devs. parameters contains half of current position of mouseX/Y
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                String mouse = String.format("mouseX: %.0f, mouseY: %.0f", MC.mouseHandler.xpos(), MC.mouseHandler.ypos());
                DrawUtils.drawText(
                        graphics,
                        mouse,
                        (int) (x - MC.font.width(mouse) / 2F),
                        (int) (y - boxCount * BOX_HEIGHT - 37),
                        ColorCode.RED.getColor(),
                        true
                );
            }

            if (lastHoveredButton == null) return;
            Feature lastHoveredButtonFeature = lastHoveredButton.getFeature();
            String featureName = lastHoveredButtonFeature.getMessage();

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                featureName += " (" + lastHoveredButtonFeature.getId() + ")";
            }

            DrawUtils.drawText(
                    graphics,
                    featureName,
                    (int) (x - MC.font.width(featureName) / 2F),
                    (int) (y - boxCount * BOX_HEIGHT - 25),
                    ColorCode.AQUA.getColor(),
                    true
            );
            String info = String.format(
                    "x=%.0f, y=%.0f, scale=%.2f",
                    lastHoveredButtonFeature.getActualX() * 2,
                    lastHoveredButtonFeature.getActualY() * 2,
                    lastHoveredButton.getScale()
            );
            DrawUtils.drawText(
                    graphics,
                    info,
                    (int) (x - MC.font.width(info) / 2F),
                    (int) (y - boxCount * BOX_HEIGHT - 12),
                    ColorCode.YELLOW.getColor(),
                    true
            );
            if (lastHoveredButtonFeature.isGuiFeature()) {
                FeatureGuiData guiFeatureData = lastHoveredButtonFeature.getFeatureGuiData();
                if (guiFeatureData.getDrawType() == DrawType.BAR) {
                    String barScales = String.format(
                            "scaleX = %.2f, scaleY = %.2f",
                            lastHoveredButton.getScaleX(),
                            lastHoveredButton.getScaleY()
                    );
                    DrawUtils.drawText(
                            graphics,
                            barScales,
                            (int) (x - MC.font.width(barScales) / 2F),
                            (int) (y - boxCount * BOX_HEIGHT),
                            ColorCode.YELLOW.getColor(),
                            true
                    );
                }
            }
        }
    }

    /**
     * If button is pressed, update the currently dragged button.
     * Otherwise, they clicked the reset button, so reset the coordinates.
     */
    private void actionPerformed(GuiEventListener eventListener, double mouseX, double mouseY, int button) {
        switch (eventListener) {
            case ButtonLocation buttonLocation -> {
                draggedFeature = buttonLocation.getFeature();
                if (draggedFeature != null) {
                    xOffset = (float) mouseX - draggedFeature.getActualX();
                    yOffset = (float) mouseY - draggedFeature.getActualY();
                }
            }
            case ButtonSolid buttonSolid -> {
                Feature feature = buttonSolid.getFeature();
                if (feature == Feature.RESET_LOCATION) {
                    main.getConfigValuesManager().setAllCoordinatesToDefault();
                    main.getConfigValuesManager().putDefaultBarSizes();
                } else if (feature == Feature.SHOW_COLOR_ICONS) {
                    Feature.SHOW_COLOR_ICONS.setEnabled(!Feature.SHOW_COLOR_ICONS.isEnabled());

                    if (Feature.SHOW_COLOR_ICONS.isEnabled()) {
                        addColorWheelsToAllFeatures();
                    } else {
                        clearAllColorWheelButtons();
                    }
                } else if (feature == Feature.ENABLE_FEATURE_SNAPPING) {
                    Feature.ENABLE_FEATURE_SNAPPING.setEnabled(!Feature.ENABLE_FEATURE_SNAPPING.isEnabled());
                }
            }
            case ButtonResize buttonResize when editMode != EditMode.NONE -> {
                draggedFeature = buttonResize.getFeature();
                resizing = true;

                if (editMode == EditMode.RESCALE_FEATURES) {
                    float scale = draggedFeature.getGuiScale();
                    xOffset = ((float) mouseX - buttonResize.resizeX * scale) / scale;
                    yOffset = ((float) mouseY - buttonResize.resizeY * scale) / scale;
                } else {
                    xOffset = (float) mouseX;
                    yOffset = (float) mouseY;
                }

                resizingCorner = buttonResize.getCorner();
            }
            default -> {
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        this.isMiddlePressed = false;

        // Reset to default scale with right mouse button click
        if (event.button() == 1) {
            ButtonLocation lastHoveredButton = resizing
                    ? buttonLocations.get(draggedFeature)
                    : getHoveredFeatureButton(event.x(), event.y());
            if (lastHoveredButton  != null) {
                main.getConfigValuesManager().putDefaultGuiScale(lastHoveredButton.getFeature());
            }
        } else if (event.button() == 2) {
            this.isMiddlePressed = true;
        }

        if (Feature.DUNGEONS_MAP_DISPLAY.isEnabled(FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD)) {
            if (SkyblockKeyBinding.DECREASE_DUNGEON_MAP_ZOOM.isDown()) {
                DungeonMapManager.decreaseZoomByStep();
            } else if (SkyblockKeyBinding.INCREASE_DUNGEON_MAP_ZOOM.isDown()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }

        Optional<GuiEventListener> optional = this.getChildAt(event.x(), event.y());
        if (optional.isEmpty()) {
            return false;
        } else {
            GuiEventListener guiEventListener = optional.get();
            if (guiEventListener.mouseClicked(event, isDoubleClick)) {
                this.setFocused(guiEventListener);
                if (event.button() == 0) {
                    this.setDragging(true);
                }
                actionPerformed(guiEventListener, event.x(), event.y(), event.button());
            }

            return true;
        }
    }

    /**
     * Allow moving the last hovered feature with arrow keys.
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        Feature hoveredFeature = ButtonLocation.getLastHoveredFeature();
        if (hoveredFeature != null) {
            int xOffset = 0;
            int yOffset = 0;
            switch (event.key()) {
                case GLFW.GLFW_KEY_LEFT -> xOffset--;
                case GLFW.GLFW_KEY_UP -> yOffset--;
                case GLFW.GLFW_KEY_RIGHT -> xOffset++;
                case GLFW.GLFW_KEY_DOWN -> yOffset++;
                case GLFW.GLFW_KEY_A -> xOffset -= 10;
                case GLFW.GLFW_KEY_W -> yOffset -= 10;
                case GLFW.GLFW_KEY_D -> xOffset += 10;
                case GLFW.GLFW_KEY_S -> yOffset += 10;
            }
            Pair<Float, Float> relativeCoords = hoveredFeature.getRelativeCoords();
            hoveredFeature.getFeatureData().setCoords(
                    relativeCoords.getLeft() + xOffset,
                    relativeCoords.getRight() + yOffset
            );
        }

        if (Feature.DUNGEONS_MAP_DISPLAY.isEnabled(FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD)) {
            if (event.key() == SkyblockKeyBinding.DECREASE_DUNGEON_MAP_ZOOM.getKeyCode()) {
                DungeonMapManager.decreaseZoomByStep();
            } else if (event.key() == SkyblockKeyBinding.INCREASE_DUNGEON_MAP_ZOOM.getKeyCode()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }

        return super.keyPressed(event);
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.isDragging()) {
            draggedFeature = null;
            resizing = false;
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(event);
            }
        }

        return this.getChildAt(event.x(), event.y())
                .filter(guiEventListener -> guiEventListener.mouseReleased(event))
                .isPresent();
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    @Override
    public void removed() {
        main.getConfigValuesManager().saveConfig();
        if (lastTab != null && !closing) {
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
        }
    }

    @Getter
    public static class Snap {

        private final Edge thisSnapEdge;
        private final Edge otherSnapEdge;
        private final float snapValue;
        private final Map<Edge, Float> rectangle = new EnumMap<>(Edge.class);

        public Snap(float left, float top, float right, float bottom, Edge thisSnapEdge, Edge otherSnapEdge, float snapValue) {
            rectangle.put(Edge.LEFT, left);
            rectangle.put(Edge.TOP, top);
            rectangle.put(Edge.RIGHT, right);
            rectangle.put(Edge.BOTTOM, bottom);

            rectangle.put(Edge.HORIZONTAL_MIDDLE, top + getHeight() / 2);
            rectangle.put(Edge.VERTICAL_MIDDLE, left + getWidth() / 2);

            this.otherSnapEdge = otherSnapEdge;
            this.thisSnapEdge = thisSnapEdge;
            this.snapValue = snapValue;
        }

        public float getHeight() {
            return rectangle.get(Edge.BOTTOM) - rectangle.get(Edge.TOP);
        }

        public float getWidth() {
            return rectangle.get(Edge.RIGHT) - rectangle.get(Edge.LEFT);
        }
    }

    public enum EditMode implements ButtonCycling.SelectItem {
        RESCALE_FEATURES("messages.rescaleFeatures"),
        RESIZE_BARS("messages.resizeBars"),
        NONE("messages.none");

        private final String TRANSLATION_KEY;

        EditMode(String translationKey) {
            this.TRANSLATION_KEY = translationKey;
        }

        @Override
        public String getDisplayName() {
            return Translations.getMessage(TRANSLATION_KEY);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

}