package com.fix3dll.skyblockaddons.core.seacreatures;


import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.fix3dll.skyblockaddons.core.SkyblockRarity.LEGENDARY;

@Getter
public class SeaCreatureManager {

    @Getter private static final SeaCreatureManager instance = new SeaCreatureManager();

    private final Set<String> allSeaCreatureSpawnMessages = new HashSet<>();
    private final Set<String> legendarySeaCreatureSpawnMessages = new HashSet<>();

    /**
     * Populate sea creature information from local and online sources
     */
    public void setSeaCreatures(Map<String, SeaCreature> seaCreatures) {
        allSeaCreatureSpawnMessages.clear();
        legendarySeaCreatureSpawnMessages.clear();
        for (SeaCreature sc : seaCreatures.values()) {
            allSeaCreatureSpawnMessages.add(sc.getSpawnMessage());
            if (sc.getRarity().compareTo(LEGENDARY) >= 0) {
                legendarySeaCreatureSpawnMessages.add(sc.getSpawnMessage());
            }
        }
    }

}