package com.fix3dll.skyblockaddons.features.dungeons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.features.dungeonmap.MapMarker;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter @Setter @ToString
public class DungeonPlayer {

    private String name;
    private DungeonClass dungeonClass;
    private int classLevel;
    private ColorCode healthColor;
    private MapMarker mapMarker;
    private int health;
    private UUID entityId;

    public DungeonPlayer(String name, DungeonClass dungeonClass, ColorCode healthColor, int health, UUID entityId) {
        this.name = name;
        this.dungeonClass = dungeonClass;
        this.classLevel = 0;
        this.healthColor = healthColor;
        this.health = health;
        this.entityId = entityId;
    }

    public boolean isLow() {
        return healthColor == ColorCode.YELLOW;
    }

    public boolean isCritical() {
        return healthColor == ColorCode.RED && health > 0;
    }

    public boolean isGhost() {
        return this.health == 0;
    }
}
