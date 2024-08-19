package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.pojo.LocationData;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum Island {
    PRIVATE_ISLAND("Private Island", "dynamic"),
    HUB("Hub", "hub"),
    GOLD_MINE("Gold Mine", "mining_1"),
    DEEP_CAVERNS("Deep Caverns", "mining_2"),
    DWARVEN_MINES("Dwarven Mines", "mining_3"),
    SPIDERS_DEN("Spider's Den", "combat_1"),
    CRIMSON_ISLE("Crimson Isle", "crimson_isle"),
    THE_END("The End", "combat_3"),
    THE_FARMING_ISLANDS("The Farming Islands", "farming_1"),
    THE_PARK("The Park", "foraging_1"),
    JERRYS_WORKSHOP("Jerry's Workshop", "winter"),
    DUNGEON("Dungeon", "dungeon"),
    DUNGEON_HUB("Dungeon Hub", "dungeon_hub"),
    CRYSTAL_HOLLOWS("Crystal Hollows", "crystal_hollows"),
    GARDEN("Garden", "garden"),
    THE_RIFT("The Rift", "rift"),
    KUUDRA("Kuudra", "kuudra"),
    MINESHAFT("Mineshaft", "mineshaft"),
    DARK_AUCTION("Dark Auction", "dark_auction"),
    UNKNOWN("null","null")
    ;

    Island(String map, String mode) {
        this.map = map;
        this.mode = mode;
        this.locationData = new LocationData();
    }

    private final String map;
    private final String mode;
    @Setter private LocationData locationData;

    public static Island getByMode(String mode) {
        for (Island island : Island.values()) {
            if (island.mode.equals(mode)) {
                return island;
            }
        }
        return Island.UNKNOWN;
    }

    public static Island getByZone(String zone) {
        for (Island island : Island.values()) {
            if (island.locationData.zones.contains(zone)) {
                return island;
            }
        }
        return Island.UNKNOWN;
    }
}
