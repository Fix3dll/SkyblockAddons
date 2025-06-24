package com.fix3dll.skyblockaddons.features.dungeonmap;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;


@Getter @Setter
public class MapMarker {

    /** The icon type of this map marker (https://minecraft.fandom.com/wiki/Map#Map_icons) */
    private byte iconType;
    private float x;
    private float z;
    private float rotation;
    private Component playerName;
    @Setter private String mapMarkerName;
    private boolean wearingHat;

    public MapMarker(Player player) {
        this.playerName = player.getName();
        this.wearingHat = player.isModelPartShown(PlayerModelPart.HAT);

        if (player == Minecraft.getInstance().player) {
            iconType = 1;
        } else {
            iconType = 3;
        }
        updateXZRot(player);
    }

    public MapMarker(byte iconType, float x, float z, float rotation) {
        this.iconType = iconType;
        this.x = x;
        this.z = z;
        this.rotation = rotation;
    }

    public Player getPlayer() {
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            for (AbstractClientPlayer player : world.players()) {
                if (player.getName().equals(playerName)) {
                    return player;
                }
            }
        }
        return null;
    }

    public void updateXZRot(Player player) {
//        x = DungeonMapManager.toMapCoordinate(player.getX(), DungeonMapManager.getMarkerOffsetX()); TODO
//        z = DungeonMapManager.toMapCoordinate(player.getZ(), DungeonMapManager.getMarkerOffsetZ());
//        rotation = MathHelper.wrapAngleTo180_float(player.getYaw()) / 360F * 16F;
    }

    @Override
    public String toString() {
        return "MapMarker{" +
                "iconType=" + iconType +
                ", x=" + x +
                ", z=" + z +
                ", rotation=" + rotation +
                ", playerName='" + playerName + '\'' +
                ", mapMarkerName='" + mapMarkerName + '\'' +
                ", wearingHat=" + wearingHat +
                '}';
    }
}
