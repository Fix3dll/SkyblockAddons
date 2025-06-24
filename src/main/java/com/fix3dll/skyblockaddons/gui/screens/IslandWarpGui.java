package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.SkyblockDate;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCustomToggle;
import com.fix3dll.skyblockaddons.gui.buttons.IslandButton;
import com.fix3dll.skyblockaddons.gui.buttons.IslandMarkerButton;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.time.Month;
import java.util.Locale;
import java.util.Optional;

public class IslandWarpGui extends SkyblockAddonsScreen {

    @Getter @Setter private static Marker doubleWarpMarker;

    private static int TOTAL_WIDTH;
    private static int TOTAL_HEIGHT;

    public static float SHIFT_LEFT;
    public static float SHIFT_TOP;

    private Marker selectedMarker;

    public static float ISLAND_SCALE;

    public IslandWarpGui() {
        super(Component.empty());
    }

    @Override
    public void init() {
        for (Island island : Island.values()) {
            if (island == Island.JERRYS_WORKSHOP
                    && main.getUtils().getCurrentDate().getMonth() != SkyblockDate.SkyblockMonth.LATE_WINTER
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final Window window = MC.getWindow();

        drawGradientBackground(graphics, Math.round(255/3F), Math.round(255/2F));

        graphics.drawCenteredString(MC.font, Translations.getMessage("warpMenu.click"), window.getGuiScaledWidth() / 2, 10, -1);
        graphics.drawCenteredString(MC.font, Translations.getMessage("warpMenu.mustUnlock"), window.getGuiScaledWidth() / 2, 20, -1);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        ISLAND_SCALE = 0.7F / 1080 * window.getHeight();
        poseStack.scale((float) (1F / window.getGuiScale()), (float) (1F / window.getGuiScale()), 1);
        poseStack.scale(ISLAND_SCALE, ISLAND_SCALE, 1);

        float totalWidth = TOTAL_WIDTH * ISLAND_SCALE;
        float totalHeight = TOTAL_HEIGHT * ISLAND_SCALE;

        SHIFT_LEFT = (window.getWidth() / 2F - totalWidth / 2F) / ISLAND_SCALE;
        SHIFT_TOP = (window.getHeight() / 2F - totalHeight / 2F) / ISLAND_SCALE;
        poseStack.translate(SHIFT_LEFT, SHIFT_TOP, 0);

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

        int x = Math.round(window.getWidth() / ISLAND_SCALE - SHIFT_LEFT - 500);
        int y = Math.round(window.getHeight() / ISLAND_SCALE - SHIFT_TOP);
        poseStack.pushPose();
        float textScale = 3F;
        poseStack.scale(textScale, textScale, 1);
        graphics.drawString(MC.font, Feature.FANCY_WARP_MENU.getMessage(), (int) (x / textScale + 50), (int) ((y - 30 - 60 * 2) / textScale + 5), -1);
        graphics.drawString(MC.font, FeatureSetting.DOUBLE_WARP.getMessage(), (int) (x / textScale + 50), (int) ((y - 30 - 60) / textScale + 5), -1);
        poseStack.popPose();

        poseStack.popPose();

        detectClosestMarker(mouseX, mouseY);
    }

    public static float IMAGE_SCALED_DOWN_FACTOR = 0.75F;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selectedMarker != null) {
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

        int i = (int)MC.mouseHandler.getScaledXPos(MC.getWindow());
        int j = (int)MC.mouseHandler.getScaledYPos(MC.getWindow());
        Pair<Integer, Integer> scaledMouseLocations = getScaledMouseLocation(i, j);
        return super.mouseClicked(scaledMouseLocations.getLeft(), scaledMouseLocations.getRight(), button);
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
        THE_END("The End", 240, 30),
        CRIMSON_ISLE("Crimson Isle", 835, 25),
        THE_PARK("The Park", 80, 440),
        SPIDERS_DEN("Spider's Den", 500, 470),
        DEEP_CAVERNS("Deep Caverns", 1400, 250),
        GOLD_MINE("Gold Mine", 1130, 525),
        MUSHROOM_DESERT("Mushroom Desert", 1470, 525),
        THE_BARN("The Barn", 1125, 850),
        HUB("Hub", 300, 820),
        PRIVATE_ISLAND("Private Island", 275, 1172),
        THE_GARDEN("The Garden", 50, 1050),
        DUNGEON_HUB("Dungeon Hub", 1500, 1100),
        JERRYS_WORKSHOP("Jerry's Workshop", 1280, 1150),
        THE_RIFT("The Rift", 1720, 1050),
        BACKWATER_BAYOU("Backwater Bayou", 960, 1275);

        private final String label;
        private final int x;
        private final int y;
        private int w;
        private int h;

        private final ResourceLocation resourceLocation;
        private NativeImage nativeImage;

        @SuppressWarnings("lossy-conversions")
        Island(String label, int x, int y) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.resourceLocation = ResourceLocation.fromNamespaceAndPath(
                    "skyblockaddons",
                    "islands/" + this.name().toLowerCase(Locale.US).replace("_", "") + ".png"
            );

            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(this.resourceLocation);
            if (resource.isPresent()) {
                try {
                    this.nativeImage = NativeImage.read(resource.get().open());
                    this.w = nativeImage.getWidth();
                    this.h = nativeImage.getHeight();

                    if (label.equals("The End")) {
                        // The original end HD texture is 573 pixels wide.
                        IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR = this.w / 573F;
                    }
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

        JERRYS_WORKSHOP("workshop", Translations.getMessage("warpMenu.spawn"), Island.JERRYS_WORKSHOP, 35, 90),

        THE_RIFT("rift", Translations.getMessage("warpMenu.spawn"), Island.THE_RIFT, 35, 90),

        BACKWATER_BAYOU("backwater", Translations.getMessage("warpMenu.spawn"), Island.BACKWATER_BAYOU, 60, 150),

        HUB("hub", Translations.getMessage("warpMenu.spawn"), Island.HUB, 610, 210),
        ELIZABETH("elizabeth", "Elizabeth", Island.HUB, 660, 150),
        CASTLE("castle", "Castle", Island.HUB, 130, 80),
        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 385, 415),
        CRYPT("crypt", "Crypts", Island.HUB, 580, 100),
        WIZARD_TOWER("wizard", "Wizard Tower", Island.HUB, 490, 260),
        MUSEUM("museum", "Museum", Island.HUB, 310, 200),
        TRADE_CENTER("stonks", "Trade Center", Island.HUB, 530, 175),
        CARNIVAL("carnival", "Carnival", Island.HUB, 480, 100),

        SPIDERS_DEN("spider", Translations.getMessage("warpMenu.spawn"), Island.SPIDERS_DEN, 345, 240),
        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 450, 30),
        ARACHNES_SANCTUARY("arachne", "Arachne's Sanctuary", Island.SPIDERS_DEN, 240, 135),

        THE_PARK("park", Translations.getMessage("warpMenu.spawn"), Island.THE_PARK, 263, 308),
        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 254, 202),
        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 194, 82),

        THE_END("end", Translations.getMessage("warpMenu.spawn"), Island.THE_END, 440, 291),
        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 248),
        VOID_SEPULTURE("void", "Void Sepulture", Island.THE_END, 370, 227),

        CRIMSON_ISLE("nether", Translations.getMessage("warpMenu.spawn"), Island.CRIMSON_ISLE, 70, 280),
        FORGOTTEN_SKULL("kuudra", "Forgotten Skull", Island.CRIMSON_ISLE, 460, 90),
        THE_WASTELAND("wasteland", "The Wasteland", Island.CRIMSON_ISLE, 330, 160),
        DRAGONTAIL("dragontail", "Dragontail", Island.CRIMSON_ISLE, 140, 150),
        SCARLETON("scarleton", "Scarleton", Island.CRIMSON_ISLE, 400, 220),
        SMOLDERING_TOMB("smold", "Smoldering Tomb", Island.CRIMSON_ISLE, 350, 70),

        THE_BARN("barn", Translations.getMessage("warpMenu.spawn"), Island.THE_BARN, 140, 150),
        MUSHROOM_DESERT("desert", Translations.getMessage("warpMenu.spawn"), Island.MUSHROOM_DESERT, 210, 295),
        TRAPPER("trapper", "Trapper's Hut", Island.MUSHROOM_DESERT, 300, 200),

        GOLD_MINE("gold", Translations.getMessage("warpMenu.spawn"), Island.GOLD_MINE, 86, 259),

        DEEP_CAVERNS("deep", Translations.getMessage("warpMenu.spawn"), Island.DEEP_CAVERNS, 97, 213),
        DWARVEN_MINES("mines", "Dwarven Mines", Island.DEEP_CAVERNS, 280, 205),
        DWARVEN_FORGE("forge", "Forge", Island.DEEP_CAVERNS, 280, 280),
        DWARVEN_BASE_CAMP("base", "Dwarven Base Camp", Island.DEEP_CAVERNS, 240, 330),
        CRYSTAL_HOLLOWS("crystals", "Crystal Hollows", Island.DEEP_CAVERNS, 190, 360),
        CRYSTAL_NUCLEUS("nucleus", "Crystal Nucleus", Island.DEEP_CAVERNS, 140, 390),

        DUNGEON_HUB_ISLAND("dungeon_hub", Translations.getMessage("warpMenu.spawn"), Island.DUNGEON_HUB, 35, 80),
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
