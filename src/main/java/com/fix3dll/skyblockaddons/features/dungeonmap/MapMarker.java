package com.fix3dll.skyblockaddons.features.dungeonmap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;


@Getter @Setter @ToString
public class MapMarker {

    /** The icon type of this map marker (https://minecraft.fandom.com/wiki/Map#Map_icons) */
    private MapDecorationType decorationType;
    private float x;
    private float y;
    private float rotation;
    private String playerName;
    @Setter private String mapMarkerName;
    private boolean wearingHat;

    public MapMarker(@NonNull Player player) {
        this.playerName = player.getGameProfile().getName();
        this.wearingHat = player.isModelPartShown(PlayerModelPart.HAT);

        if (player == Minecraft.getInstance().player) {
            decorationType = MapDecorationTypes.FRAME.value();
        } else {
            decorationType = MapDecorationTypes.BLUE_MARKER.value();
        }
        updateXZRot(player);
    }

    public MapMarker(MapDecorationType decorationType, float x, float y, float rotation) {
        this.decorationType = decorationType;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public Player getPlayer() {
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            for (AbstractClientPlayer player : world.players()) {
                if (player.getGameProfile().getName().equals(playerName)) {
                    return player;
                }
            }
        }
        return null;
    }

    public void updateXZRot(@NonNull Player player) {
        x = DungeonMapManager.toMapCoordinate(player.getX(), DungeonMapManager.getMarkerOffsetX());
        y = DungeonMapManager.toMapCoordinate(player.getZ(), DungeonMapManager.getMarkerOffsetZ());
        rotation = Mth.wrapDegrees(player.getYRot()) / 360F * 16F;
    }

}