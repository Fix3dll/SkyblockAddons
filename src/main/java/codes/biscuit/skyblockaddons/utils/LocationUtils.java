package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Location;

import java.util.EnumSet;

/**
 * Class used to help with certain location places that has sub-locations like the Dwarven Mines
 */
public class LocationUtils {
    // List of sublocations of the Dwarven Mines
    private static final EnumSet<Location> dwarvenLocations = EnumSet.of(Location.DWARVEN_MINES, Location.DWARVEN_VILLAGE,
            Location.GATES_TO_THE_MINES, Location.THE_LIFT, Location.THE_FORGE, Location.FORGE_BASIN, Location.LAVA_SPRINGS,
            Location.PALACE_BRIDGE, Location.ROYAL_PALACE, Location.ARISTOCRAT_PASSAGE, Location.HANGING_TERRACE, Location.CLIFFSIDE_VEINS,
            Location.RAMPARTS_QUARRY, Location.DIVANS_GATEWAY, Location.FAR_RESERVE, Location.GOBLIN_BURROWS, Location.UPPER_MINES,
            Location.ROYAL_MINES, Location.MINERS_GUILD, Location.GREAT_ICE_WALL, Location.THE_MIST, Location.CC_MINECARTS_CO,
            Location.GRAND_LIBRARY, Location.HANGING_COURT);
   // List of sublocations of the Crystal Hollows
    private static final EnumSet<Location> hollowsLocations = EnumSet.of(Location.MAGMA_FIELDS,
           Location.CRYSTAL_HOLLOWS, Location.CRYSTAL_NUCLEUS, Location.JUNGLE, Location.MITHRIL_DEPOSITS, Location.GOBLIN_HOLDOUT,
           Location.PRECURSOR_REMNANT, Location.FAIRY_GROTTO, Location.KHAZAD_DUM, Location.JUNGLE_TEMPLE, Location.MINES_OF_DIVAN,
           Location.GOBLIN_QUEEN_DEN, Location.LOST_PRECURSOR_CITY);
    // List of sublocations of the Mushroom Desert
    private static final EnumSet<Location> mushroomDesertLocations = EnumSet.of(Location.MUSHROOM_DESERT, Location.TRAPPERS_DEN,
            Location.DESERT_SETTLEMENT, Location.GLOWING_MUSHROOM_CAVE, Location.MUSHROOM_GORGE, Location.OVERGROWN_MUSHROOM_CAVE,
            Location.DESERT_MOUNTAIN, Location.SHEPHERDS_KEEP, Location.OASIS, Location.JAKES_HOUSE, Location.TREASURE_HUNTER_CAMP);
    // List of sublocations of the Spiders Den
    private static final EnumSet<Location> spidersDenLocations = EnumSet.of(Location.SPIDERS_DEN, Location.SPIDER_MOUND,
            Location.ARACHNES_SANCTUARY, Location.ARACHNES_BURROW, Location.GRANDMAS_HOUSE, Location.ARCHAEOLOGISTS_CAMP, Location.GRAVEL_MINES);
    // List of sublocations of The End
    private static final EnumSet<Location> theEndLocations = EnumSet.of(Location.THE_END, Location.DRAGONS_NEST,
            Location.VOID_SEPULTURE, Location.ZEALOT_BRUISER_HIDEOUT, Location.VOID_SLATE);
    // List of sublocations of the Winter Island
    private static final EnumSet<Location> winterIslandLocations = EnumSet.of(Location.JERRYS_WORKSHOP, Location.JERRY_POND,
            Location. MOUNT_JERRY, Location. GARYS_SHACK, Location.GLACIAL_CAVE, Location.TERRYS_SHACK, Location.HOT_SPRINGS,
            Location.REFLECTIVE_POND, Location.SUNKEN_JERRY_POND, Location.SHERRYS_SHOWROOM, Location. EINARYS_EMPORIUM);
    // List of locations that spawn zealots/zealot variants
    private static final EnumSet<Location> zealotSpawnLocations = EnumSet.of(Location.DRAGONS_NEST, Location.ZEALOT_BRUISER_HIDEOUT);

    /**
     * @param location current location
     * @return true if this sublocation is located within Dwarven Mines location
     */
    public static boolean isInDwarvenMines(Location location) {
        return dwarvenLocations.contains(location);
    }

    /**
     * @param location current location
     * @return true if this sublocation is located within Crystal Hollows location
     */
    public static boolean isInCrystalHollows(Location location) {
        return hollowsLocations.contains(location);
    }

    /**
     * @param location current location
     * @return true if this sublocation is located within Spiders Den location
     */
    public static boolean isInSpidersDen(Location location) {
        return spidersDenLocations.contains(location);
    }

    /**
     * @param location current location
     * @return true if this sublocation is located within The End location
     */
    public static boolean isInTheEnd(Location location) {
        return theEndLocations.contains(location);
    }

    /**
     * @param location current location
     * @return true if this sublocation is located within Winter Island location
     */
    public static boolean isInWinterIsland(Location location) {
        return winterIslandLocations.contains(location);
    }
    /**
     * @param location current location
     * @return true if this sublocation is located within Mushroom Desert
     */
    public static boolean isInMushroomDesert(Location location) {
        return mushroomDesertLocations.contains(location);
    }
    /**
     * @param location current location
     * @return true if this sublocation is located within zealot spawns location
     */
    public static boolean isZealotSpawnLocation(Location location) {
        return zealotSpawnLocations.contains(location);
    }
}
