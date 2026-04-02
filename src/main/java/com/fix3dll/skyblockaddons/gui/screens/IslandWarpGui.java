package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockDate;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCustomToggle;
import com.fix3dll.skyblockaddons.gui.buttons.IslandButton;
import com.fix3dll.skyblockaddons.gui.buttons.IslandMarkerButton;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.Month;
import java.util.Locale;
import java.util.Optional;

public class IslandWarpGui extends SkyblockAddonsScreen {

    @Getter @Setter private static Marker doubleWarpMarker;

    private static final Window WINDOW = MC.getWindow();

    public static float IMAGE_SCALED_DOWN_FACTOR = 0.75F;

    private static int TOTAL_WIDTH;
    private static int TOTAL_HEIGHT;

    public static float SHIFT_LEFT;
    public static float SHIFT_TOP;

    public static Marker selectedMarker;

    public static float ISLAND_SCALE;

    public IslandWarpGui() {
        super(Component.empty());
    }

    @Override
    public void init() {
        super.init();
        selectedMarker = null;

        for (Island island : Island.values()) {
            if (island == Island.JERRYS_WORKSHOP
                    && main.getUtils().getCurrentDate().month() != SkyblockDate.SkyblockMonth.LATE_WINTER
                    && SkyblockAddons.getHypixelZonedDateTime().getMonth() != Month.DECEMBER) {
                continue;
            }
            addRenderableWidget(new IslandButton(island));
        }

        int screenWidth = MC.getWindow().getWidth();
        int screenHeight = MC.getWindow().getHeight();

        ISLAND_SCALE = 0.7F / 1080 * screenHeight;

        float totalWidth = TOTAL_WIDTH * ISLAND_SCALE;
        float totalHeight = TOTAL_HEIGHT * ISLAND_SCALE;
        SHIFT_LEFT = (screenWidth / 2F - totalWidth / 2F) / ISLAND_SCALE;
        SHIFT_TOP = (screenHeight / 2F - totalHeight / 2F) / ISLAND_SCALE;

        int x = Math.round(screenWidth / ISLAND_SCALE - SHIFT_LEFT - 475);
        int y = Math.round(screenHeight / ISLAND_SCALE - SHIFT_TOP);

        addRenderableWidget(new ButtonCustomToggle(x, y - 30 - 60 * 2, 50,
                Feature.FANCY_WARP_MENU::isEnabled,
                () -> Feature.FANCY_WARP_MENU.setEnabled(Feature.FANCY_WARP_MENU.isDisabled())));
        addRenderableWidget(new ButtonCustomToggle(x, y - 30 - 60, 50,
                () -> Feature.FANCY_WARP_MENU.isEnabled(FeatureSetting.DOUBLE_WARP),
                () -> Feature.FANCY_WARP_MENU.set(
                        FeatureSetting.DOUBLE_WARP,
                        Feature.FANCY_WARP_MENU.isDisabled(FeatureSetting.DOUBLE_WARP)
                )
        ));
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.nextStratum();
        drawGradientBackground(graphics, Math.round(255/3F), Math.round(255/2F));

        graphics.drawCenteredString(MC.font, Translations.getMessage("warpMenu.click"), WINDOW.getGuiScaledWidth() / 2, 10, -1);
        graphics.drawCenteredString(MC.font, Translations.getMessage("warpMenu.mustUnlock"), WINDOW.getGuiScaledWidth() / 2, 20, -1);

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        ISLAND_SCALE = 0.7F / 1080 * WINDOW.getHeight();
        poseStack.scale(1F / WINDOW.getGuiScale());
        poseStack.scale(ISLAND_SCALE);

        float totalWidth = TOTAL_WIDTH * ISLAND_SCALE;
        float totalHeight = TOTAL_HEIGHT * ISLAND_SCALE;

        SHIFT_LEFT = (WINDOW.getWidth() / 2F - totalWidth / 2F) / ISLAND_SCALE;
        SHIFT_TOP = (WINDOW.getHeight() / 2F - totalHeight / 2F) / ISLAND_SCALE;
        poseStack.translate(SHIFT_LEFT, SHIFT_TOP);

        IslandButton lastHoveredButton = null;

        for (Renderable button : this.renderables) {
            if (button instanceof IslandButton islandButton) {
                // Call this just so it calculates the hover, don't actually draw.
                islandButton.drawButton(graphics, mouseX, mouseY, false);

                if (islandButton.isHovering()) {
                    if (lastHoveredButton != null) {
                        lastHoveredButton.setDisableHover(true);
                    }
                    lastHoveredButton = islandButton;
                }
            }
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        int x = Math.round(WINDOW.getWidth() / ISLAND_SCALE - SHIFT_LEFT - 500);
        int y = Math.round(WINDOW.getHeight() / ISLAND_SCALE - SHIFT_TOP);
        poseStack.pushMatrix();
        float textScale = 3F;
        poseStack.scale(textScale);
        graphics.drawString(MC.font, Feature.FANCY_WARP_MENU.getMessage(), (int) (x / textScale + 50), (int) ((y - 30 - 60 * 2) / textScale + 5), -1);
        graphics.drawString(MC.font, FeatureSetting.DOUBLE_WARP.getMessage(), (int) (x / textScale + 50), (int) ((y - 30 - 60) / textScale + 5), -1);
        poseStack.popMatrix();

        poseStack.popMatrix();

        detectClosestMarker(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && selectedMarker != null) {
            MC.setScreen(null);

            if (Feature.FANCY_WARP_MENU.isEnabled(FeatureSetting.DOUBLE_WARP)) {
                doubleWarpMarker = selectedMarker;

                // Remove the marker if it didn't trigger for some reason...
                main.getScheduler().scheduleTask(scheduledTask -> {
                    if (doubleWarpMarker != null) {
                        doubleWarpMarker = null;
                    }
                }, 20);
            }
            if (selectedMarker != null && MC.player != null) {
                MC.player.connection.sendChat("/warp " + selectedMarker.getWarpName());
            }

        }

        Pair<Integer, Integer> scaledMouseLocations = getScaledMouseLocation((int) event.x(), (int) event.y());
        MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(
                scaledMouseLocations.getLeft(), scaledMouseLocations.getRight(), event.buttonInfo()
        );
        for (GuiEventListener guiEventListener : this.children()) {
            if (guiEventListener.isMouseOver(scaledMouseLocations.getLeft(), scaledMouseLocations.getRight())) {
                if (guiEventListener.mouseClicked(mouseButtonEvent, isDoubleClick)) {
                    this.setFocused(guiEventListener);
                    if (event.button() == 0) {
                        this.setDragging(true);
                    }
                }

                return true;
            }
        }

        return false;
    }

    public void detectClosestMarker(int mouseX, int mouseY) {
        Pair<Integer, Integer> scaledMouseLocations = getScaledMouseLocation(mouseX, mouseY);

        Marker hoveredMarker = null;
        double markerDistance = IslandMarkerButton.MAX_SELECT_RADIUS + 1;

        for (Renderable button : this.renderables) {
            if (button instanceof IslandButton islandButton) {
                for (IslandMarkerButton marker : islandButton.getMarkerButtons()) {
                    double distance = marker.getDistance(
                            scaledMouseLocations.getLeft(), // x
                            scaledMouseLocations.getRight() // y
                    );

                    if (distance != -1 && distance < markerDistance) {
                        hoveredMarker = marker.getMarker();
                        markerDistance = distance;
                    }
                }
            }
        }

        selectedMarker = hoveredMarker;

        //if (hoveredMarker != null) System.out.println(hoveredMarker.getLabel()+" "+markerDistance);
    }

    /**
     * Returns a scaled X,Y pair after using {@link IslandWarpGui#ISLAND_SCALE} for scaling
     * <br> See Also: {@link IslandWarpGui#SHIFT_LEFT} {@link IslandWarpGui#SHIFT_TOP}
     * @param mouseX current mouseX
     * @param mouseY current mouseY
     * @return scaled X, Y {@link Pair}
     */
    @SuppressWarnings("lossy-conversions")
    public static Pair<Integer, Integer> getScaledMouseLocation(int mouseX, int mouseY) {
        double minecraftScale = MC.getWindow().getGuiScale();
        float islandGuiScale = IslandWarpGui.ISLAND_SCALE;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        return new Pair<>(mouseX, mouseY);
    }

    @Getter
    public enum Island {
        THE_END("The End", 350, 20),
        CRIMSON_ISLE("Crimson Isle", 960, 90),
        THE_PARK("The Park", 160, 440),
        SPIDERS_DEN("Spider's Den", 740, 470),
        DEEP_CAVERNS("Deep Caverns", 1610, 245),
        THE_FORGE("The Forge", 2260, 218), // exception
        DWARVEN_MINES("Dwarven Mines", 2060, 340),
        DWARVEN_BASE_CAMP("Dwarven Base Camp", 2260, 462),
        CRYSTAL_HOLLOWS("Crystal Hollows", 2460, 584),
        GOLD_MINE("Gold Mine", 1360, 500),
        MUSHROOM_DESERT("Mushroom Desert", 1830, 600),
        THE_BARN("The Barn", 1410, 870),
        HUB("Hub", 510, 820),
        PRIVATE_ISLAND("Private Island", 385, 1172),
        THE_GARDEN("The Garden", 160, 1050),
        DUNGEON_HUB("Dungeon Hub", 1890, 1117),
        JERRYS_WORKSHOP("Jerry's Workshop", 1660, 1239),
        THE_RIFT("The Rift", 2120, 995),
        BACKWATER_BAYOU("Backwater Bayou", 1285, 1250),
        GALATEA("Galatea", -140, 240);

        private final String label;
        private final int x;
        private final int y;
        private int w;
        private int h;

        private final Identifier identifier;
        private NativeImage nativeImage;

        @SuppressWarnings("lossy-conversions")
        Island(String label, int x, int y) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.identifier = SkyblockAddons.identifier(
                    "islands/" + this.name().toLowerCase(Locale.US).replace("_", "") + ".png"
            );

            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(this.identifier);
            if (resource.isPresent()) {
                try {
                    this.nativeImage = NativeImage.read(resource.get().open());
                    this.w = nativeImage.getWidth();
                    this.h = nativeImage.getHeight();
                } catch (IOException e) {
                    SkyblockAddons.getLogger().catching(e);
                }
            }

            this.w /= IMAGE_SCALED_DOWN_FACTOR;
            this.h /= IMAGE_SCALED_DOWN_FACTOR;

            if (this.y + this.h > TOTAL_HEIGHT) {
                TOTAL_HEIGHT = this.y + this.h;
            }
            if (this.x + this.w > TOTAL_WIDTH) {
                TOTAL_WIDTH = this.x + this.w;
            }
        }
    }

    //TODO: Maybe change these to load from a file at some point
    @Getter
    public enum Marker {
        PRIVATE_ISLAND("home", Translations.getMessage("warpMenu.home"), Island.PRIVATE_ISLAND, 72, 90),

        THE_GARDEN("garden", Translations.getMessage("warpMenu.spawn"), Island.THE_GARDEN, 160, 70),

        JERRYS_WORKSHOP("workshop", "", Island.JERRYS_WORKSHOP, 35, 90),

        THE_RIFT("rift", "", Island.THE_RIFT, 35, 90),

        BACKWATER_BAYOU("backwater", Translations.getMessage("warpMenu.spawn"), Island.BACKWATER_BAYOU, 40, 200),

        HUB("hub", Translations.getMessage("warpMenu.spawn"), Island.HUB, 630, 230),
        ELIZABETH("elizabeth", "Elizabeth", Island.HUB, 700, 180),
        CASTLE("castle", "Castle", Island.HUB, 130, 80),
        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 445, 430),
        CRYPT("crypt", "Crypts", Island.HUB, 440, 80),
        WIZARD_TOWER("wizard", "Wizard Tower", Island.HUB, 510, 270),
        MUSEUM("museum", "Museum", Island.HUB, 320, 240),
        TRADE_CENTER("stonks", "Trade Center", Island.HUB, 540, 175),
        CARNIVAL("carnival", "Carnival", Island.HUB, 440, 160),

        SPIDERS_DEN("spider", Translations.getMessage("warpMenu.spawn"), Island.SPIDERS_DEN, 315, 250),
        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 390, 50),
        ARACHNES_SANCTUARY("arachne", "Arachne's Sanctuary", Island.SPIDERS_DEN, 220, 135),

        THE_PARK("park", Translations.getMessage("warpMenu.spawn"), Island.THE_PARK, 420, 350),
        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 310, 180),
        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 150, 100),

        GALATEA("galatea", Translations.getMessage("warpMenu.spawn"), Island.GALATEA, 160, 260),
        MURKWATER_LOCH("murkwater", "Murkwater Loch", Island.GALATEA, 220, 140),

        THE_END("end", Translations.getMessage("warpMenu.spawn"), Island.THE_END, 440, 291),
        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 240),
        VOID_SEPULTURE("void", "Void Sepulture", Island.THE_END, 370, 200),

        CRIMSON_ISLE("nether", Translations.getMessage("warpMenu.spawn"), Island.CRIMSON_ISLE, 70, 280),
        FORGOTTEN_SKULL("kuudra", "Forgotten Skull", Island.CRIMSON_ISLE, 450, 100),
        THE_WASTELAND("wasteland", "The Wasteland", Island.CRIMSON_ISLE, 330, 160),
        DRAGONTAIL("dragontail", "Dragontail", Island.CRIMSON_ISLE, 140, 150),
        SCARLETON("scarleton", "Scarleton", Island.CRIMSON_ISLE, 400, 220),
        SMOLDERING_TOMB("smoldering", "Smoldering Tomb", Island.CRIMSON_ISLE, 480, 140),

        THE_BARN("barn", Translations.getMessage("warpMenu.spawn"), Island.THE_BARN, 140, 150),
        MUSHROOM_DESERT("desert", Translations.getMessage("warpMenu.spawn"), Island.MUSHROOM_DESERT, 250, 320),
        TRAPPER("trapper", "Trapper's Hut", Island.MUSHROOM_DESERT, 185, 85),
        MOBYS_SHOP("glowing", "Moby's Shop", Island.MUSHROOM_DESERT, 165, 165),

        GOLD_MINE("gold", Translations.getMessage("warpMenu.spawn"), Island.GOLD_MINE, 86, 259),

        DEEP_CAVERNS("deep", Translations.getMessage("warpMenu.spawn"), Island.DEEP_CAVERNS, 90, 213),
        CRYSTAL_NUCLEUS("nucleus", "Crystal Nucleus", Island.DEEP_CAVERNS, 150, 380),

        THE_FORGE("forge", "", Island.THE_FORGE, 35, 90), // Exception

        DWARVEN_MINES("mines", "", Island.DWARVEN_MINES, 36, 90),

        DWARVEN_BASE_CAMP("base", "", Island.DWARVEN_BASE_CAMP, 38, 90),

        CRYSTAL_HOLLOWS("crystals", "", Island.CRYSTAL_HOLLOWS, 35, 90),

        DUNGEON_HUB_ISLAND("dungeon_hub", "", Island.DUNGEON_HUB, 35, 90),
        ;

        private final String warpName;
        private final String label;
        private final Island island;
        private final boolean advanced;
        private final int x;
        private final int y;

        Marker(String warpName, String label, Island island, int x, int y) {
            this(warpName, label, island, false, x, y);
        }

        Marker(String warpName, String label, Island island, boolean advanced, int x, int y) {
            this.warpName = warpName;
            this.label = label;
            this.island = island;
            this.x = x;
            this.y = y;
            this.advanced = advanced;
        }
    }

}