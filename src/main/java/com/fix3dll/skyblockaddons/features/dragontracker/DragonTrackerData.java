package com.fix3dll.skyblockaddons.features.dragontracker;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
public class DragonTrackerData {

    private final List<DragonType> recentDragons = new LinkedList<>();
    private final Map<DragonsSince, Integer> dragonsSince = new EnumMap<>(DragonsSince.class);
    @Setter private int eyesPlaced = 0;

}