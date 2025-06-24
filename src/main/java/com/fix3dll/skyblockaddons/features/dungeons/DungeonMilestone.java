package com.fix3dll.skyblockaddons.features.dungeons;

import lombok.Getter;

@Getter
public class DungeonMilestone {

    private final DungeonClass dungeonClass;
    private final String level;
    private final String value;

    public DungeonMilestone(DungeonClass dungeonClass) {
        this(dungeonClass, "⓿", "0");
    }

    public DungeonMilestone(DungeonClass dungeonClass, String level, String value) {
        this.dungeonClass = dungeonClass;
        this.level = level;
        this.value = value;
    }
}
