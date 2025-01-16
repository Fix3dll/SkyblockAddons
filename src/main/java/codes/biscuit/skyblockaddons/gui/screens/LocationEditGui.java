package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureGuiData;
import codes.biscuit.skyblockaddons.core.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonCycling;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonColorWheel;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonResize;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSolid;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class LocationEditGui extends SkyblockAddonsScreen {

    private static final int BOX_HEIGHT = 20;
    private static final int SNAPPING_RADIUS = 120;
    private static final int SNAP_PULL = 1;

    @Getter private static EditMode editMode = EditMode.RESCALE_FEATURES;

    /** The feature that is currently being dragged, or null for nothing. */
    private Feature draggedFeature;
    @Getter private static boolean resizing;
    private ButtonResize.Corner resizingCorner;

    private float xOffset;
    private float yOffset;

    @Getter private final int lastPage;
    @Getter private final EnumUtils.GuiTab lastTab;

    private final Map<Feature, ButtonLocation> buttonLocations = new EnumMap<>(Feature.class);

    @Setter private boolean closing = false;
    private boolean rightClickReleased = true;
    private static boolean tipShown = false;

    public LocationEditGui(int lastPage, EnumUtils.GuiTab lastTab) {
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @Override
    public void initGui() {
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            // Don't display features that have been disabled
            if (feature.isGuiFeature() && feature.isEnabled()) {
                ButtonLocation buttonLocation = new ButtonLocation(feature);
                buttonList.add(buttonLocation);
                buttonLocations.put(feature, buttonLocation);
            }
        }

        if (editMode != EditMode.NONE) {
            addResizeButtons();
        }
        if (Feature.SHOW_COLOR_ICONS.isEnabled()) {
            addColorWheelsToAllFeatures();
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        Set<Feature> guiFeatures = Feature.getEditGuiFeatures();
        guiFeatures.removeIf(Feature::isRemoteDisabled);
        int numButtons = guiFeatures.size();
        int x;
        int y = scaledResolution.getScaledHeight()/2;
        // List may change later
        //noinspection ConstantConditions
        if (numButtons % 2 == 0) {
            y -= Math.round((numButtons/2F) * (BOX_HEIGHT+5)) - 5;
        } else {
            y -= Math.round(((numButtons-1)/2F) * (BOX_HEIGHT+5)) + 20;
        }

        for (Feature feature : guiFeatures) {
            String featureName = feature.getMessage();
            int boxWidth = feature == Feature.RESCALE_FEATURES
                    ? SkyblockAddonsGui.BUTTON_MAX_WIDTH
                    : mc.fontRendererObj.getStringWidth(featureName) + 10;
            if (boxWidth > SkyblockAddonsGui.BUTTON_MAX_WIDTH) boxWidth = SkyblockAddonsGui.BUTTON_MAX_WIDTH;
            x = scaledResolution.getScaledWidth() / 2 - boxWidth / 2;
            y += BOX_HEIGHT + 5;

            if (feature == Feature.RESCALE_FEATURES) {
                buttonList.add(new ButtonCycling(x, y, boxWidth, BOX_HEIGHT, Arrays.asList(EditMode.values()), editMode.ordinal(), index -> {
                    editMode = EditMode.values()[index];
                    closing = true;
                    mc.displayGuiScreen(new LocationEditGui(lastPage, lastTab));
                    closing = false;
                    addResizeButtons();
                }));
            } else if (feature == Feature.RESET_LOCATION) {
                buttonList.add(new ButtonSolid(x, y, boxWidth, BOX_HEIGHT, featureName, feature, 0xFF7878,false));
            } else {
                buttonList.add(new ButtonSolid(x, y, boxWidth, BOX_HEIGHT, featureName, feature, true));
            }
        }

        // 1 tip per session :)
        if (mc.thePlayer != null && !tipShown) {
            tipShown = true;
            main.getUtils().sendMessage(ColorCode.GREEN + Translations.getMessage("messages.locationEditGui.atOpening"));
        }
    }

    private void addResizeButtons() {
        clearAllResizeButtons();
        switch (editMode) {
            case RESIZE_BARS:
                // Add all gui elements that can be edited to the gui.
                for (Feature feature : Feature.getGuiFeatures()) {
                    // Don't display features that have been disabled
                    if (feature.isEnabled()) {
                        FeatureGuiData guiFeatureData = feature.getFeatureGuiData();
                        if (guiFeatureData != null && guiFeatureData.getDrawType() == EnumUtils.DrawType.BAR) {
                            addResizeCorners(feature);
                        }
                    }
                }
                break;
            case RESCALE_FEATURES:
                // Add all gui elements that can be edited to the gui.
                for (Feature feature : Feature.getGuiFeatures()) {
                    if (feature.isEnabled()) { // Don't display features that have been disabled
                        addResizeCorners(feature);
                    }
                }
                break;
            case NONE:
                break;
        }
    }

    private void clearAllResizeButtons() {
        buttonList.removeIf((button) -> button instanceof ButtonResize);
    }

    private void clearAllColorWheelButtons() {
        buttonList.removeIf((button) -> button instanceof ButtonColorWheel);
    }

    private void addColorWheelsToAllFeatures() {
        for (ButtonLocation buttonLocation : buttonLocations.values()) {
            Feature feature = buttonLocation.getFeature();

            if (feature.getFeatureGuiData() == null || feature.getFeatureGuiData().getDefaultColor() == null) {
                continue;
            }

            float scaleX = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
            float scaleY = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesY() : 1;
            float boxXOne = buttonLocation.getBoxXOne() * scaleX;
            float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
            float boxYOne = buttonLocation.getBoxYOne() * scaleY;
            float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;
            float y = boxYOne + (boxYTwo - boxYOne) / 2F - ButtonColorWheel.SIZE / 2F;
            float x;

            if (main.getConfigValuesManager().getAnchorPoint(feature).isOnLeft()) {
                x = boxXTwo + 2;
            } else {
                x = boxXOne - ButtonColorWheel.SIZE - 2;
            }

            buttonList.add(new ButtonColorWheel(Math.round(x), Math.round(y), feature));
        }
    }

    private void addResizeCorners(Feature feature) {
        buttonList.removeIf(
                button -> button instanceof ButtonResize && ((ButtonResize)button).getFeature() == feature
        );

        ButtonLocation buttonLocation = buttonLocations.get(feature);
        if (buttonLocation == null) {
            return;
        }

        float scale = buttonLocation.getScale();
        float scaledX1 = buttonLocation.getBoxXOne() * scale * buttonLocation.getScaleX();
        float scaledY1 = buttonLocation.getBoxYOne() * scale * buttonLocation.getScaleY();
        float scaledX2 = buttonLocation.getBoxXTwo() * scale * buttonLocation.getScaleX();
        float scaledY2 = buttonLocation.getBoxYTwo() * scale * buttonLocation.getScaleY();
        buttonList.add(new ButtonResize(scaledX1, scaledY1, feature, ButtonResize.Corner.TOP_LEFT));
        buttonList.add(new ButtonResize(scaledX2, scaledY1, feature, ButtonResize.Corner.TOP_RIGHT));
        buttonList.add(new ButtonResize(scaledX1, scaledY2, feature, ButtonResize.Corner.BOTTOM_LEFT));
        buttonList.add(new ButtonResize(scaledX2, scaledY2, feature, ButtonResize.Corner.BOTTOM_RIGHT));
    }

    /**
     * @return {@code ButtonLocation} the mouse is currently hovering over or {@code null} if the mouse is not hovering
     * over any
     */
    private ButtonLocation getHoveredFeatureButton() {
        ButtonLocation lastHovered = null;
        for (ButtonLocation buttonLocation : buttonLocations.values()) {
            if (buttonLocation.isMouseOver()) {
                lastHovered = buttonLocation;
            }
        }
        return lastHovered;
    }

    private void recalculateResizeButtons() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof ButtonResize) {
                ButtonResize buttonResize = (ButtonResize) button;
                ButtonResize.Corner corner = buttonResize.getCorner();
                Feature feature = buttonResize.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    continue;
                }

                float scaleX = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
                float scaleY = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesY() : 1;
                float boxXOne = buttonLocation.getBoxXOne() * scaleX;
                float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
                float boxYOne = buttonLocation.getBoxYOne() * scaleY;
                float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;

                if (corner == ButtonResize.Corner.TOP_LEFT) {
                    buttonResize.x = boxXOne;
                    buttonResize.y = boxYOne;
                } else if (corner == ButtonResize.Corner.TOP_RIGHT) {
                    buttonResize.x = boxXTwo;
                    buttonResize.y = boxYOne;
                } else if (corner == ButtonResize.Corner.BOTTOM_LEFT) {
                    buttonResize.x = boxXOne;
                    buttonResize.y = boxYTwo;
                } else if (corner == ButtonResize.Corner.BOTTOM_RIGHT) {
                    buttonResize.x = boxXTwo;
                    buttonResize.y = boxYTwo;
                }
            }
        }
    }

    private void recalculateColorWheels() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof ButtonColorWheel) {
                ButtonColorWheel buttonColorWheel = (ButtonColorWheel) button;
                Feature feature = buttonColorWheel.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    continue;
                }

                float scaleX = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesX() : 1;
                float scaleY = feature.getFeatureGuiData().getDrawType() == EnumUtils.DrawType.BAR ? feature.getFeatureData().getSizesY() : 1;
                float boxXOne = buttonLocation.getBoxXOne() * scaleX;
                float boxXTwo = buttonLocation.getBoxXTwo() * scaleX;
                float boxYOne = buttonLocation.getBoxYOne() * scaleY;
                float boxYTwo = buttonLocation.getBoxYTwo() * scaleY;
                float y = boxYOne + (boxYTwo - boxYOne) / 2F - ButtonColorWheel.SIZE / 2F;
                float x;

                if (main.getConfigValuesManager().getAnchorPoint(feature).isOnLeft()) {
                    x = boxXTwo + 2;
                } else {
                    x = boxXOne - ButtonColorWheel.SIZE - 2;
                }

                buttonColorWheel.x = x;
                buttonColorWheel.y = y;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Snap[] snaps = checkSnapping();

        onMouseMove(mouseX, mouseY, snaps);

        if (editMode != EditMode.NONE) {
            recalculateResizeButtons();
        }
        if (Feature.SHOW_COLOR_ICONS.isEnabled()) {
            recalculateColorWheels();
        }
        drawGradientBackground(64, 128);

        Feature lastHoveredFeature = ButtonLocation.getLastHoveredFeature();

        for (EnumUtils.AnchorPoint anchorPoint : EnumUtils.AnchorPoint.values()) {
            ScaledResolution sr = new ScaledResolution(mc);
            int x = anchorPoint.getX(sr.getScaledWidth());
            int y = anchorPoint.getY(sr.getScaledHeight());
            int color = ColorCode.RED.getColor(127);
            if (lastHoveredFeature != null && main.getConfigValuesManager().getAnchorPoint(lastHoveredFeature) == anchorPoint) {
                color = ColorCode.YELLOW.getColor(127);
            }
            DrawUtils.drawRectAbsolute(x-4, y-4, x+4, y+4, color);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.

        if (snaps != null) {
            for (Snap snap : snaps) {
                if (snap != null) {
                    float left = snap.getRectangle().get(Edge.LEFT);
                    float top = snap.getRectangle().get(Edge.TOP);
                    float right = snap.getRectangle().get(Edge.RIGHT);
                    float bottom = snap.getRectangle().get(Edge.BOTTOM);

                    if (snap.getWidth() < 0.5) {
                        float averageX = (left+right)/2;
                        left = averageX-0.25F;
                        right = averageX+0.25F;
                    }
                    if (snap.getHeight() < 0.5) {
                        float averageY = (top+bottom)/2;
                        top = averageY-0.25F;
                        bottom = averageY+0.25F;
                    }

                    if ((right-left) == 0.5 || (bottom-top) == 0.5) {
                        DrawUtils.drawRectAbsolute(left, top, right, bottom, 0xFF00FF00);
                    } else {
                        DrawUtils.drawRectAbsolute(left, top, right, bottom, 0xFFFF0000);
                    }
                }
            }
        }

        ButtonLocation lastHoveredButton = resizing ? buttonLocations.get(draggedFeature) : getHoveredFeatureButton();

        // Rescaling features with mouse buttons
        listenRescaleButtons(lastHoveredButton);

        // Draw location information of hovered feature to the middle of screen
        drawFeatureCoords(lastHoveredButton);
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
                                    if (Feature.DEVELOPER_MODE.isEnabled()) {
                                        DrawUtils.drawRectAbsolute(snapX - 0.5, 0, snapX + 0.5, mc.displayHeight, 0xFF0000FF);
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
                                    if (Feature.DEVELOPER_MODE.isEnabled()) {
                                        DrawUtils.drawRectAbsolute(0, snapY - 0.5, mc.displayWidth, snapY + 0.5, 0xFF0000FF);
                                    }
                                    verticalSnap = thisSnap;
                                }
                            }
                        }
                    }
                }
            }

            return new Snap[] {horizontalSnap, verticalSnap};
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

        @Getter private static final Set<Edge> verticalEdges = Sets.newHashSet(TOP, BOTTOM, HORIZONTAL_MIDDLE);
        @Getter private static final Set<Edge> horizontalEdges = Sets.newHashSet(LEFT, RIGHT, VERTICAL_MIDDLE);

        public float getCoordinate(ButtonLocation button) {
            switch (this) {
                case LEFT:
                    return button.getBoxXOne() * button.getScale() * button.getScaleX();
                case TOP:
                    return button.getBoxYOne() * button.getScale() * button.getScaleY();
                case RIGHT:
                    return button.getBoxXTwo() * button.getScale() * button.getScaleX();
                case BOTTOM:
                    return button.getBoxYTwo() * button.getScale() * button.getScaleY();
                case HORIZONTAL_MIDDLE:
                    return TOP.getCoordinate(button) + (BOTTOM.getCoordinate(button) - TOP.getCoordinate(button)) / 2F;
                case VERTICAL_MIDDLE:
                    return LEFT.getCoordinate(button) + (RIGHT.getCoordinate(button) - LEFT.getCoordinate(button)) / 2F;
                default:
                    return 0;
            }
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    protected void onMouseMove(int mouseX, int mouseY, Snap[] snaps) {
        ButtonLocation buttonLocation = buttonLocations.get(draggedFeature);
        if (buttonLocation == null) {
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

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

                buttonLocation.drawButton(mc, mouseX, mouseY);
                recalculateResizeButtons();

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
                    buttonLocation.drawButton(mc, mouseX, mouseY);
                    recalculateResizeButtons();
                }
            }
        } else if (draggedFeature != null) {
            Snap horizontalSnap = null;
            Snap verticalSnap = null;
            if (snaps != null) {
                horizontalSnap = snaps[0];
                verticalSnap = snaps[1];
            }

            float x = floatMouseX - draggedFeature.getAnchorPoint().getX(sr.getScaledWidth());
            float y = floatMouseY - draggedFeature.getAnchorPoint().getY(sr.getScaledHeight());

            boolean xSnapped = false;
            boolean ySnapped = false;

            if (horizontalSnap != null) {
                float snapX = horizontalSnap.getSnapValue();

                if (horizontalSnap.getThisSnapEdge() == Edge.LEFT) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX + scaledWidth/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(sr.getScaledWidth()) + scaledWidth/2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.RIGHT) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX - scaledWidth/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(sr.getScaledWidth()) - scaledWidth/2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.VERTICAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - draggedFeature.getAnchorPoint().getX(sr.getScaledWidth());
                    }
                }
            }

            if (verticalSnap != null) {
                float snapY = verticalSnap.getSnapValue();

                if (verticalSnap.getThisSnapEdge() == Edge.TOP) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY + scaledHeight/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(sr.getScaledHeight()) + scaledHeight/2F;
                    }

                } else if (verticalSnap.getThisSnapEdge() == Edge.BOTTOM) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY - scaledHeight/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(sr.getScaledHeight()) - scaledHeight/2F;
                    }
                } else if (verticalSnap.getThisSnapEdge() == Edge.HORIZONTAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - draggedFeature.getAnchorPoint().getY(sr.getScaledHeight());
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
                float xChange = Math.abs(main.getConfigValuesManager().getRelativeCoords(draggedFeature).getLeft() - x);
                float yChange = Math.abs(main.getConfigValuesManager().getRelativeCoords(draggedFeature).getRight() - y);
                if (xChange < 0.001 && yChange < 0.001) {
                    return;
                }
            }

            draggedFeature.getFeatureData().setCoords(x, y);
            main.getConfigValuesManager().setClosestAnchorPoint(draggedFeature);
            switch (draggedFeature) {
                case HEALTH_BAR:
                case MANA_BAR:
                case DRILL_FUEL_BAR:
                    if (editMode != EditMode.NONE) {
                        addResizeCorners(draggedFeature);
                    }
                    break;
            }
        }
    }

    private void listenRescaleButtons(ButtonLocation lastHoveredButton) {
        if (lastHoveredButton == null || editMode != EditMode.RESCALE_FEATURES) return;

        Feature lastHoveredFeature = lastHoveredButton.getFeature();
        if (lastHoveredFeature == null) return;

        float oldScale = lastHoveredFeature.getGuiScale();

        // Rescale feature with mouse scroll
        int wheel = Mouse.getDWheel();
        if (wheel > 0) {
            float newScale = oldScale + (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1.0F : 0.1F);
            lastHoveredFeature.setGuiScale(newScale);
            recalculateResizeButtons();
        } else if (wheel < 0) {
            float newScale = oldScale - (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1.0F : 0.1F);
            lastHoveredFeature.setGuiScale(newScale);
            recalculateResizeButtons();
        }

        // Reset to default scale with right mouse button click
        if (Mouse.isButtonDown(1) && rightClickReleased) {
            rightClickReleased = false;
            main.getConfigValuesManager().putDefaultGuiScale(lastHoveredFeature);
        }
    }

    private void drawFeatureCoords(ButtonLocation lastHoveredButton) {
        if (editMode != EditMode.NONE) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            final double x = scaledResolution.getScaledWidth_double() / 2D;
            final double y = scaledResolution.getScaledHeight_double() / 2D;
            final int boxCount = (int) Math.ceil(Feature.getEditGuiFeatures().size() / 2D);

            // mouseX/Y for devs. parameters contains half of current position of mouseX/Y
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                String mouse = String.format("mouseX: %d, mouseY: %d", Mouse.getX(), mc.displayHeight - Mouse.getY());
                mc.fontRendererObj.drawStringWithShadow(
                        mouse,
                        (float) x - mc.fontRendererObj.getStringWidth(mouse) / 2F,
                        (float) y - boxCount * BOX_HEIGHT - 37,
                        ColorCode.RED.getColor()
                );
            }

            if (lastHoveredButton == null) return;
            Feature lastHoveredButtonFeature = lastHoveredButton.getFeature();
            String featureName = lastHoveredButtonFeature.getMessage();

            if (Feature.DEVELOPER_MODE.isEnabled()) {
                featureName += " (" + lastHoveredButtonFeature.getId() + ")";
            }

            mc.fontRendererObj.drawStringWithShadow(
                    featureName,
                    (float) x - mc.fontRendererObj.getStringWidth(featureName) / 2F,
                    (float) y - boxCount * BOX_HEIGHT - 25,
                    ColorCode.AQUA.getColor()
            );
            String info = String.format(
                    "x=%.0f, y=%.0f, scale=%.2f",
//                    lastHoveredButton.getBoxXOne() * lastHoveredButton.getScale() * 2,
//                    lastHoveredButton.getBoxYOne() * lastHoveredButton.getScale() * 2,
                    main.getConfigValuesManager().getActualX(lastHoveredButtonFeature) * 2,
                    main.getConfigValuesManager().getActualY(lastHoveredButtonFeature) * 2,
                    lastHoveredButton.getScale()
            );
            mc.fontRendererObj.drawStringWithShadow(
                    info,
                    (float) x - mc.fontRendererObj.getStringWidth(info) / 2F,
                    (float) y - boxCount * BOX_HEIGHT - 12,
                    ColorCode.YELLOW.getColor()
            );
            if (lastHoveredButtonFeature.isGuiFeature()) {
                FeatureGuiData guiFeatureData = lastHoveredButtonFeature.getFeatureGuiData();
                if (guiFeatureData.getDrawType() == EnumUtils.DrawType.BAR) {
                    String barScales = String.format(
                            "scaleX = %.2f, scaleY = %.2f",
                            lastHoveredButton.getScaleX(),
                            lastHoveredButton.getScaleY()
                    );
                    mc.fontRendererObj.drawStringWithShadow(
                            barScales,
                            (float) x - mc.fontRendererObj.getStringWidth(barScales) / 2F,
                            (float) y - boxCount * BOX_HEIGHT,
                            ColorCode.YELLOW.getColor()
                    );
                }
            }
        }
    }

    /**
     * If button is pressed, update the currently dragged button.
     * Otherwise, they clicked the reset button, so reset the coordinates.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLocation) {
            ButtonLocation buttonLocation = (ButtonLocation) abstractButton;
            draggedFeature = buttonLocation.getFeature();

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            xOffset = floatMouseX - main.getConfigValuesManager().getActualX(buttonLocation.getFeature());
            yOffset = floatMouseY - main.getConfigValuesManager().getActualY(buttonLocation.getFeature());
        } else if (abstractButton instanceof ButtonSolid) {
            ButtonSolid buttonSolid = (ButtonSolid) abstractButton;
            Feature feature = buttonSolid.getFeature();
            if (feature == Feature.RESET_LOCATION) {
                main.getConfigValuesManager().setAllCoordinatesToDefault();
                main.getConfigValuesManager().putDefaultBarSizes();
                for (Feature guiFeature : Feature.getGuiFeatures()) {
                    // Don't display features that have been disabled
                    switch (guiFeature) {
                        case HEALTH_BAR:
                        case MANA_BAR:
                        case DRILL_FUEL_BAR:
                            if (guiFeature.isEnabled() && editMode != EditMode.NONE) {
                                addResizeCorners(guiFeature);
                            }
                    }
                }
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
        } else if (editMode != EditMode.NONE && abstractButton instanceof ButtonResize) {
            ButtonResize buttonResize = (ButtonResize) abstractButton;
            draggedFeature = buttonResize.getFeature();
            resizing = true;

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            if (editMode == EditMode.RESCALE_FEATURES) {
                float scale = buttonResize.getFeature().getGuiScale();
                xOffset = (floatMouseX - buttonResize.getX() * scale) / scale;
                yOffset = (floatMouseY - buttonResize.getY() * scale) / scale;
            } else {
                xOffset = floatMouseX;
                yOffset = floatMouseY;
            }

            resizingCorner = buttonResize.getCorner();
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

    /**
     * Allow moving the last hovered feature with arrow keys.
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        Feature hoveredFeature = ButtonLocation.getLastHoveredFeature();
        if (hoveredFeature != null) {
            int xOffset = 0;
            int yOffset = 0;
            if (keyCode == Keyboard.KEY_LEFT) {
                xOffset--;
            } else if (keyCode == Keyboard.KEY_UP) {
                yOffset--;
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                xOffset++;
            } else if (keyCode == Keyboard.KEY_DOWN) {
                yOffset++;
            }
            if (keyCode == Keyboard.KEY_A) {
                xOffset-= 10;
            } else if (keyCode == Keyboard.KEY_W) {
                yOffset-= 10;
            } else if (keyCode == Keyboard.KEY_D) {
                xOffset+= 10;
            } else if (keyCode == Keyboard.KEY_S) {
                yOffset+= 10;
            }
            hoveredFeature.getFeatureData().setCoords(
                    main.getConfigValuesManager().getRelativeCoords(hoveredFeature).getLeft() + xOffset,
                    main.getConfigValuesManager().getRelativeCoords(hoveredFeature).getRight() + yOffset
            );
        }

        if (Feature.DUNGEONS_MAP_DISPLAY.isEnabled(FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD)) {
            if (keyCode == SkyblockKeyBinding.DECREASE_DUNGEON_MAP_ZOOM.getKeyCode()) {
                DungeonMapManager.decreaseZoomByStep();
            } else if (keyCode == SkyblockKeyBinding.INCREASE_DUNGEON_MAP_ZOOM.getKeyCode()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggedFeature = null;
        resizing = false;
        rightClickReleased = true;
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValuesManager().saveConfig();
        if (lastTab != null && !closing) {
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
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
            return "";
        }
    }

}