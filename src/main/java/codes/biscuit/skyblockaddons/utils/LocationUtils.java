package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Class used to help with certain location places that has sub-locations like the Dwarven Mines
 */
public class LocationUtils {
    @Setter private static HashMap<String, List<String>> slayerLocations;

    /**
     * List of locations that spawn zealots/zealot variants
     */
    private static final List<String> zealotSpawnLocations = Collections.unmodifiableList(
            Arrays.asList("Dragon's Nest", "Zealot Bruiser Hideout")
    );

    /**
     * List of Skyblock zones where we might see items in showcases from outside. We don't outline showcase blocks
     * while the player is in this area to avoid disturbing players.
     */
    @Getter
    private static final List<String> showcaseLocations = Collections.unmodifiableList(
            Arrays.asList("Village", "Auction House", "Bazaar Alley", "Library", "Jerry's Workshop")
    );

    /**
     * @param location current location
     * @return true if this sublocation is located within zealot spawns location
     */
    public static boolean isZealotSpawnLocation(String location) {
        return zealotSpawnLocations.contains(location);
    }

    /**
     * @param slayerQuest slayer type
     * @param location current location
     * @return true if it is the location where the given slayer type is counted
     */
    public static boolean isSlayerLocation(EnumUtils.SlayerQuest slayerQuest, String location) {
        return slayerLocations.get(slayerQuest.name()).contains(location);
    }
}
