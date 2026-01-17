package com.fix3dll.skyblockaddons.features.dungeons;

public record DungeonMilestone(DungeonClass dungeonClass, String level, String value) {

    public DungeonMilestone(DungeonClass dungeonClass) {
        this(dungeonClass, "⓿", "0");
    }

}