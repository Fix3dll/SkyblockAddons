package com.fix3dll.skyblockaddons.features.slayertracker;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class SlayerTrackerData {

    private final Map<SlayerBoss, Integer> slayerKills = new EnumMap<>(SlayerBoss.class);
    private final Map<SlayerDrop, Integer> slayerDropCounts = new EnumMap<>(SlayerDrop.class);
    @Setter private SlayerBoss lastKilledBoss;

}