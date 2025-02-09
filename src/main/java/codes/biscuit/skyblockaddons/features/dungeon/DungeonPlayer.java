package codes.biscuit.skyblockaddons.features.dungeon;

import codes.biscuit.skyblockaddons.features.dungeonmap.MapMarker;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class DungeonPlayer {

    private String name;
    private DungeonClass dungeonClass;
    private int classLevel;
    private ColorCode healthColor;
    private MapMarker mapMarker;
    private int health;

    public DungeonPlayer(String name, DungeonClass dungeonClass, ColorCode healthColor, int health) {
        this.name = name;
        this.dungeonClass = dungeonClass;
        this.classLevel = 0;
        this.healthColor = healthColor;
        this.health = health;
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