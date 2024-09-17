package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Class used to help with certain location places that has sub-locations like the Dwarven Mines
 */
// TODO special zones could be add to data repository as constant
public class LocationUtils {
    @Setter private static HashMap<String, Set<String>> slayerLocations;

    /**
     * List of locations that spawn zealots/zealot variants
     */
    private static final Set<String> zealotSpawnLocations = Collections.unmodifiableSet(
            Sets.newHashSet("Dragon's Nest", "Zealot Bruiser Hideout")
    );

    /**
     * List of SkyBlock locations where we might see items in showcases from outside. We don't outline showcase blocks
     * while the player is in this area to avoid disturbing players.
     */
    @Getter
    private static final Set<String> showcaseLocations = Collections.unmodifiableSet(
            Sets.newHashSet("Village", "Auction House", "Bazaar Alley", "Library", "Jerry's Workshop")
    );

    /**
     * List of locations that counts in Glacite Tunnels
     */
    private static final Set<String> glaciteTunnelsLocations = Collections.unmodifiableSet(
            Sets.newHashSet("Dwarven Base Camp", "Fossil Research Center", "Glacite Tunnels", "Great Glacite Lake")
    );

    /**
     * @return true if current location is where zealot spawns
     */
    public static boolean isOnZealotSpawnLocation() {
        return zealotSpawnLocations.contains(SkyblockAddons.getInstance().getUtils().getLocation());
    }

    /**
     * @param slayerQuest slayer type
     * @return true if current location where the given slayer type is counted
     */
    public static boolean isOnSlayerLocation(EnumUtils.SlayerQuest slayerQuest) {
        return slayerLocations.get(slayerQuest.name()).contains(SkyblockAddons.getInstance().getUtils().getLocation());
    }

    /**
     * @return true if current location is where counts in Glacite Tunnels
     */
    public static boolean isOnGlaciteTunnelsLocation() {
        return glaciteTunnelsLocations.contains(SkyblockAddons.getInstance().getUtils().getLocation());
    }

    /**
     * @param islands Islands to check if the player is on it
     * @return true if current map is one of the specified islands
     */
    public static boolean isOn(Island... islands) {
        if (islands == null || islands.length == 0) {
            throw new IllegalArgumentException("\"islands\" cannot be null or empty");
        }

        Island currentIsland = SkyblockAddons.getInstance().getUtils().getMap();
        for (Island island : islands) {
            if (currentIsland == island) return true;
        }
        return false;
    }

    /**
     * @param locations Locations to check if the player is on it
     * @return true if current location is one of the specified locations
     */
    public static boolean isOn(String... locations) {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("\"locations\" cannot be null or empty");
        }

        String currentLocation = SkyblockAddons.getInstance().getUtils().getLocation();
        for (String location : locations) {
            if (currentLocation.equals(location)) return true;
        }
        return false;
    }
}
