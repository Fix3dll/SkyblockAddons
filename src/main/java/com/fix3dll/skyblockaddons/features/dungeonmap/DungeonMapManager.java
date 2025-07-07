package com.fix3dll.skyblockaddons.features.dungeonmap;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonPlayer;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonLocation;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public class DungeonMapManager {

    private static final Minecraft MC = Minecraft.getInstance();

    public static final float MIN_ZOOM = 0.5F;
    public static final float MAX_ZOOM = 5F;
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation DUNGEON_MAP = SkyblockAddons.resourceLocation("dungeonsmap.png");
    private static final Comparator<MapMarker> MAP_MARKER_COMPARATOR = (first, second) -> {
        boolean firstIsNull = first.getMapMarkerName() == null;
        boolean secondIsNull = second.getMapMarkerName() == null;

        if (!Objects.equals(first.getDecorationType(), second.getDecorationType())) {
            int firstId = BuiltInRegistries.MAP_DECORATION_TYPE.getId(first.getDecorationType());
            int secondId = BuiltInRegistries.MAP_DECORATION_TYPE.getId(second.getDecorationType());
            return Integer.compare(firstId, secondId);
        }

        if (firstIsNull && secondIsNull) {
            return 0;
        } else if (firstIsNull) {
            return 1;
        } else if (secondIsNull) {
            return -1;
        }

        return second.getMapMarkerName().compareTo(first.getMapMarkerName());
    };

    /** The factor the player's coordinates are multiplied by to calculate their map marker coordinates */
    private static final float COORDINATE_FACTOR = 1.33F;

    private static MapItemSavedData mapData;
    private static MapId mapId;
    /** The offset added to the player's x-coordinate when calculating their map marker coordinates */
    @Getter private static double markerOffsetX = 0;
    /** The offset added to the player's z-coordinate when calculating their map marker coordinates */
    @Getter private static double markerOffsetZ = 0;
    private static final NavigableMap<Long, Vec3> previousLocations = new TreeMap<>();

    public static void drawDungeonsMap(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation == null && !main.getUtils().isInDungeon()) {
            markerOffsetX = -1;
            markerOffsetZ = -1;
            mapData = null;
        }

        ItemStack possibleMapItemStack = MC.player == null ? null : MC.player.getInventory().getItem(8);
        if (buttonLocation == null && (possibleMapItemStack == null || possibleMapItemStack.getItem() != Items.FILLED_MAP
                || possibleMapItemStack.getCustomName() == null) && mapData == null) {
            return;
        }
        boolean isScoreSummary;
        if (buttonLocation == null && possibleMapItemStack != null && possibleMapItemStack.getItem() == Items.FILLED_MAP) {
            String customName = possibleMapItemStack.getCustomName().getString();
            isScoreSummary = customName.contains("Your Score Summary");

            if (!customName.contains("Magical Map") && !isScoreSummary) {
                return;
            }
        } else {
            isScoreSummary = false;
        }

        float x = Feature.DUNGEONS_MAP_DISPLAY.getActualX();
        float y = Feature.DUNGEONS_MAP_DISPLAY.getActualY();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        final int originalSize = 128;
        final float initialScaleFactor = 0.5F;

        int size = (int) (originalSize * initialScaleFactor);

        int minecraftScale = (int) MC.getWindow().getGuiScale();
        int scissorX = Math.round((x - size / 2f * scale) * minecraftScale);
        int scissorY = MC.getWindow().getHeight() - Math.round((y + size / 2F * scale) * minecraftScale);
        int widthHeight = Math.round(size * minecraftScale * scale);
        // Scissor is in screen coordinates...
        graphics.flush();
        RenderSystem.enableScissor(scissorX, scissorY, widthHeight, widthHeight);

        x = transformXY(x, size, scale);
        y = transformXY(y, size, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + size, y, y + size, scale);
        }
        Feature feature = Feature.DUNGEONS_MAP_DISPLAY;

        graphics.flush();
        RenderSystem.disableScissor();
        DrawUtils.fillAbsolute(graphics, x, y, x + size, y + size, 0x55000000);
        ManualChromaManager.renderingText(feature);
        int color = feature.getColor();
        RenderType renderType = feature.isChroma() && Feature.CHROMA_MODE.getValue() == ChromaMode.FADE
                ? DrawUtils.CHROMA_STANDARD
                : RenderType.gui();
        DrawUtils.renderOutlineAbsolute(graphics, renderType, x, y, size, size, 1, color);
        ManualChromaManager.doneRenderingText();
        graphics.flush();
        RenderSystem.enableScissor(scissorX, scissorY, widthHeight, widthHeight);

        float zoomScaleFactor = isScoreSummary ? 1.0F : getMapZoom();
        float totalScaleFactor = initialScaleFactor * zoomScaleFactor;
        float mapSize = (originalSize * totalScaleFactor);

        poseStack.scale(totalScaleFactor, totalScaleFactor, 1);
        x /= totalScaleFactor;
        y /= totalScaleFactor;
        poseStack.translate(x, y, 0);

        float rotationCenterX = originalSize * initialScaleFactor;
        float rotationCenterY = originalSize * initialScaleFactor;

        float centerOffset = -((mapSize - size) / zoomScaleFactor);
        poseStack.translate(centerOffset, centerOffset, 0);

        boolean rotateOnPlayer = feature.isEnabled(FeatureSetting.CENTER_ROTATION_ON_PLAYER);
        boolean rotate = !isScoreSummary && feature.isEnabled(FeatureSetting.ROTATE_MAP);

        if (buttonLocation == null) {
            try {
                boolean foundMapData;
                MapItemSavedData newMapData;
                MapId newMapId;
                if (possibleMapItemStack != null) {
                    newMapData = MapItem.getSavedData(possibleMapItemStack, MC.level);
                    newMapId = possibleMapItemStack.get(DataComponents.MAP_ID);
                } else {
                    newMapData = null;
                    newMapId = null;
                }
                if (newMapData != null && newMapId != null) {
                    mapData = newMapData;
                    mapId = newMapId;
                    foundMapData = true;
                } else {
                    foundMapData = false;
                }

                if (mapData != null && mapId != null) {
                    // TODO Feature Rewrite: Replace with per-tick service...
                    long now = System.currentTimeMillis();
                    previousLocations.entrySet().removeIf(entry -> entry.getKey() < now - 1000);
                    Vec3 currentVector = MC.player.position();
                    previousLocations.put(now, currentVector);

                    double lastSecondTravel = -1;
                    Map.Entry<Long, Vec3> closestEntry = previousLocations.ceilingEntry(now - 1000);
                    if (closestEntry != null) {
                        Vec3 lastSecondVector = closestEntry.getValue();
                        if (lastSecondVector != null) {
                            lastSecondTravel = lastSecondVector.distanceTo(currentVector);
                        }
                    }
                    if (foundMapData && ((markerOffsetX == -1 || markerOffsetZ == -1) || lastSecondTravel == 0)) {
                        mapData.getDecorations().forEach(mapDecoration -> {
                            // Icon type 1 is the green player marker...
                            if (mapDecoration.type().value() == MapDecorationTypes.FRAME.value()) {
                                markerOffsetX = calculateMarkerOffset(MC.player.xLast, mapDecoration.x());
                                markerOffsetZ = calculateMarkerOffset(MC.player.zLast, mapDecoration.y());
                            }
                        });
                    }

                    if (rotate && rotateOnPlayer) {
                        rotationCenterX = toRenderCoordinate(toMapCoordinate(MC.player.getX(), markerOffsetX));
                        rotationCenterY = toRenderCoordinate(toMapCoordinate(MC.player.getZ(), markerOffsetZ));
                    }

                    if (rotate) {
                        if (rotateOnPlayer) {
                            poseStack.translate(size - rotationCenterX, size - rotationCenterY, 0);
                        }

                        float rotation;
                        if (MC.player != null) {
                            rotation = 180F - Mth.wrapDegrees(MC.player.getYRot());
                        } else {
                            rotation = 0; // Dummy
                        }

                        poseStack.translate(rotationCenterX, rotationCenterY, 0);
                        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
                        poseStack.translate(-rotationCenterX, -rotationCenterY, 0);
                    }
                    graphics.drawSpecial(source -> drawMap(graphics, source, mapData, mapId, isScoreSummary, zoomScaleFactor));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (rotate) {
                float ticks = System.currentTimeMillis() % 18000 / 50F;

                poseStack.translate(rotationCenterX, rotationCenterY, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(ticks));
                poseStack.translate(-rotationCenterX, -rotationCenterY, 0);
            }

            graphics.blit(RenderType::guiTextured, DUNGEON_MAP, 0, 0, 0, 0, 128, 128, 128, 128);
        }
        graphics.flush();
        RenderSystem.disableScissor();
        poseStack.popPose();
    }


    private static final LinkedHashMap<String, MapDecoration> savedMapDecorations = Maps.newLinkedHashMap();

    private static void drawMap(GuiGraphics graphics, MultiBufferSource bufferSource, MapItemSavedData mapData, MapId mapId, boolean isScoreSummary, float markerScale) {
        PoseStack poseStack = graphics.pose();
        Matrix4f matrix4f = poseStack.last().pose();
        ResourceLocation texture = MC.getMapTextureManager().prepareMapTexture(mapId , mapData);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.textSeeThrough(texture));
        vertexConsumer.addVertex(matrix4f, 0.0F, 128.0F, -0.01F).setColor(-1).setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 128.0F, 128.0F, -0.01F).setColor(-1).setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 128.0F, 0.0F, -0.01F).setColor(-1).setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0.0F, 0.0F, -0.01F).setColor(-1).setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT);
        int decorationCount = 0;

        // We don't need to show any markers...
        if (isScoreSummary) return;

        // Prevent marker flickering...
        savedMapDecorations.clear();
        savedMapDecorations.putAll(mapData.decorations);

        // Don't add markers that we replaced with smooth client side ones
        ObjectOpenHashSet<String> dontAddMarkerNames = new ObjectOpenHashSet<>();

        // The final set of markers that will be used
        TreeSet<MapMarker> allMarkers = new TreeSet<>(MAP_MARKER_COMPARATOR);

        // Grab all the teammates and try to correlate them to the map
        Object2ObjectOpenHashMap<String, DungeonPlayer> teammates = main.getDungeonManager().getTeammates();
        ObjectIterator<DungeonPlayer> iterator = teammates.values().iterator();
        while (MC.level != null) {
            DungeonPlayer teammate;
            if (iterator.hasNext()) {
                teammate = iterator.next();
            } else {
                teammate = null;
            }

            MapMarker playerMarker;
            if (teammate != null && MC.level.getPlayerByUUID(teammate.getEntityId()) instanceof Player player) {
                playerMarker = getMapMarkerForPlayer(teammate, player);
            } else {
                playerMarker = getMapMarkerForPlayer(null, MC.player);
            }

            if (playerMarker != null) {
                if (playerMarker.getMapMarkerName() != null) {
                    dontAddMarkerNames.add(playerMarker.getMapMarkerName());
                }
                allMarkers.add(playerMarker);
            }

            if (teammate == null) break;
        }

        // Grab all the map icons to make sure we don't miss any that weren't correlated before
        for (Map.Entry<String, MapDecoration> decoration : savedMapDecorations.entrySet()) {
            String string = decoration.getKey();
            MapDecoration mapDecoration = decoration.getValue();
            // If we replaced this marker with a smooth one OR this is the player's marker, lets skip.
            if (dontAddMarkerNames.contains(string) || mapDecoration.type().value() == MapDecorationTypes.FRAME.value()) {
                continue;
            }

            // Check if this marker key is linked to a player
            DungeonPlayer foundDungeonPlayer = null;
            boolean linkedToPlayer = false;
            for (DungeonPlayer dungeonPlayer : teammates.values()) {
                if (dungeonPlayer.getMapMarker() != null) {
                    String mapMarkerName = dungeonPlayer.getMapMarker().getMapMarkerName();
                    if (mapMarkerName != null && mapMarkerName.equals(string)) {
                        linkedToPlayer = true;
                        foundDungeonPlayer = dungeonPlayer;
                        break;
                    }
                }
            }

            // If this isn't linked to a player, lets just add the marker normally...
            if (!linkedToPlayer) {
                allMarkers.add(
                        new MapMarker(mapDecoration.type().value(), mapDecoration.x(), mapDecoration.y(), mapDecoration.rot())
                );
            } else {
                // This marker is linked to a player, lets update that marker's data to the server's
                MapMarker mapMarker = foundDungeonPlayer.getMapMarker();
                mapMarker.setX(mapDecoration.x());
                mapMarker.setY(mapDecoration.y());
                mapMarker.setRotation(mapDecoration.rot());
                allMarkers.add(mapMarker);
            }
        }

        markerScale = 4.0F / markerScale;

        for (MapMarker marker : allMarkers) {
            String markerName = marker.getPlayerName();
            boolean showPlayerHead = Feature.DUNGEONS_MAP_DISPLAY.isEnabled(FeatureSetting.SHOW_PLAYER_HEADS_ON_MAP);

            PlayerInfo markerPlayerInfo = null;
            if (showPlayerHead && markerName != null) {
                for (PlayerInfo playerInfo : MC.getConnection().getOnlinePlayers()) {
                    if (markerName.equals(playerInfo.getProfile().getName())) {
                        markerPlayerInfo = playerInfo;
                        break;
                    }
                }
            }

            poseStack.pushPose();
            poseStack.translate(marker.getX() / 2.0F + 64.0F, marker.getY() / 2.0F + 64.0F, -0.02F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(marker.getRotation() * 360 / 16.0F));
            poseStack.scale(markerScale, markerScale, 3.0F);

            if (markerPlayerInfo != null) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
                DrawUtils.fillAbsolute(graphics, -1.2F, -1.2F, 1.2F, 1.2F, 0xFF000000);

                int color = -1;
                if (Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled() && teammates.containsKey(markerName)) {
                    DungeonPlayer dungeonPlayer = teammates.get(markerName);
                    if (dungeonPlayer.isLow()) {
                        color = ARGB.colorFromFloat(1F, 1F, 1F, 0.5F);
                    } else if (dungeonPlayer.isCritical()) {
                        color = ARGB.colorFromFloat(1F, 1F, 0.5F, 0.5F);
                    }
                }

                ResourceLocation skin = markerPlayerInfo.getSkin().texture();
                poseStack.pushPose();
                poseStack.scale(0.25F, 0.25F, 1F);
                graphics.blit(RenderType::guiTexturedOverlay, skin, -4, -4, 8.0F, 8, 8, 8, 8, 8, 64, 64, color);
                if (markerPlayerInfo.showHat()) {
                    graphics.blit(RenderType::guiTexturedOverlay, skin, -4, -4, 40.0F, 8, 8, 8, 8, 8, 64, 64, color);
                }
                poseStack.popPose();
            } else {
                poseStack.translate(-0.125F, 0.125F, 0.0F);
                TextureAtlasSprite textureAtlasSprite;
                if (marker.getDecorationType() != null) {
                    textureAtlasSprite = MC.getMapDecorationTextures().getSprite(marker.getDecorationType().assetId());
                } else {
                    textureAtlasSprite = null;
                }

                if (textureAtlasSprite != null) {
                    VertexConsumer vertexConsumer2 = bufferSource.getBuffer(RenderType.textSeeThrough(textureAtlasSprite.atlasLocation()));
                    Matrix4f matrix4f2 = poseStack.last().pose();
                    vertexConsumer2.addVertex(matrix4f2, -1.0F, 1.0F, decorationCount * -0.001F)
                            .setColor(-1)
                            .setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0())
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer2.addVertex(matrix4f2, 1.0F, 1.0F, decorationCount * -0.001F)
                            .setColor(-1)
                            .setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0())
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer2.addVertex(matrix4f2, 1.0F, -1.0F, decorationCount * -0.001F)
                            .setColor(-1)
                            .setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1())
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer2.addVertex(matrix4f2, -1.0F, -1.0F, decorationCount * -0.001F)
                            .setColor(-1)
                            .setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1())
                            .setLight(LightTexture.FULL_BRIGHT);
                }
            }
            poseStack.popPose();
            decorationCount++;
        }
    }

    public static MapMarker getMapMarkerForPlayer(DungeonPlayer dungeonPlayer, Player player) {
        MapMarker mapMarker;
        if (dungeonPlayer != null) {
            // If this player's marker already exists, lets update the saved one instead
            if (dungeonPlayer.getMapMarker() == null) {
                dungeonPlayer.setMapMarker(mapMarker = new MapMarker(player));
            } else {
                mapMarker = dungeonPlayer.getMapMarker();
                mapMarker.updateXZRot(player);
            }
        } else {
            mapMarker = new MapMarker(player);
        }

        // Check if there is a vanilla marker around the same spot as our custom
        // marker. If so, we probably found the corresponding marker for this player.
        int duplicates = 0;
        Map.Entry<String, MapDecoration> duplicate = null;
        for (Map.Entry<String, MapDecoration> decoration : savedMapDecorations.entrySet()) {
            MapDecoration mapDecoration = decoration.getValue();
            if (mapDecoration.type().value() == mapMarker.getDecorationType() &&
                    Math.abs(mapDecoration.x() - mapMarker.getX()) <= 5 &&
                    Math.abs(mapDecoration.y() - mapMarker.getY()) <= 5) {
                duplicates++;
                duplicate = decoration;
            }
        }

        // However, if we find more than one duplicate marker, we can't be
        // certain that this we found the player's corresponding marker.
        if (duplicates == 1) {
            mapMarker.setMapMarkerName(duplicate.getKey());
        }

        return mapMarker;
    }

    /**
     * Calculates {@code markerOffsetX} or {@code markerOffsetZ}.
     * @param playerCoordinate the player's x/z coordinate from {@link LocalPlayer#position()}
     * @param playerMarkerCoordinate the player's map marker x/z coordinate from {@code mapData}
     * @return the x/z offset used to calculate the player marker's coordinates
     */
    public static double calculateMarkerOffset(double playerCoordinate, int playerMarkerCoordinate) {
        return BigDecimal.valueOf(playerMarkerCoordinate - (COORDINATE_FACTOR * playerCoordinate))
                .setScale(5, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Converts a player's actual x/z coordinate to their marker's x/z coordinate on the dungeon map.
     * The resulting coordinate is rounded to the nearest integer lower than the actual value.
     * @param playerCoordinate the player's x/z coordinate from {@link LocalPlayer#getX()} or {@link LocalPlayer#getZ()}
     * @param markerOffset {@code markerOffsetX} or {@code markerOffsetZ}
     * @return the player marker's x/z coordinate
     */
    public static float toMapCoordinate(double playerCoordinate, double markerOffset) {
        return BigDecimal.valueOf((COORDINATE_FACTOR * playerCoordinate) + markerOffset)
                .setScale(5, RoundingMode.HALF_UP)
                .floatValue();
    }

    /**
     * Converts a map marker's x/z coordinate to the screen coordinate used when rendering it.
     *
     * @param mapCoordinate the map marker's x/z coordinate
     * @return the screen coordinate used when rendering the map marker
     */
    public static float toRenderCoordinate(float mapCoordinate) {
        return mapCoordinate / 2.0F + 64.0F;
    }

    /**
     * Increases the zoom level of the dungeon map by 0.1
     */
    public static void increaseZoomByStep() {
        setMapZoom(getMapZoom() + 0.1F);
    }

    /**
     * Decreases the zoom level of the dungeon map by 0.1
     */
    public static void decreaseZoomByStep() {
        setMapZoom(getMapZoom() - 0.1F);
    }

    /**
     * Returns Dungeon Map Zoom value.
     * @return Returns Dungeon Map Zoom value.
     */
    public static float getMapZoom() {
        return Feature.DUNGEONS_MAP_DISPLAY.getAsNumber(FeatureSetting.DUNGEON_MAP_ZOOM).floatValue();
    }

    /**
     * Saves Dungeon Map Zoom value.
     * @param value float value of Dungeon Zoom
     */
    public static void setMapZoom(float value) {
        Feature.DUNGEONS_MAP_DISPLAY.set(
                FeatureSetting.DUNGEON_MAP_ZOOM,
                Math.max(Math.min(value, MAX_ZOOM), MIN_ZOOM)
        );
        main.getConfigValuesManager().saveConfig();
    }

    private static float transformXY(float xy, int widthHeight, float scale) {
        float minecraftScale = (float) MC.getWindow().getGuiScale();
        xy -= widthHeight / 2F * scale;
        xy = Math.round(xy * minecraftScale) / minecraftScale;
        return xy / scale;
    }

}