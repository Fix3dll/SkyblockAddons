package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Location;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.EnumSet;

/**
 * Class used to help with certain location places that has sub-locations like the Dwarven Mines
 */
public class LocationUtils {
    /**
     * List of Dwarven Mines zones
     */
    private static final EnumSet<Location> dwarvenLocations = EnumSet.of(Location.DWARVEN_MINES, Location.DWARVEN_VILLAGE,
            Location.GATES_TO_THE_MINES, Location.THE_LIFT, Location.THE_FORGE, Location.FORGE_BASIN, Location.LAVA_SPRINGS,
            Location.PALACE_BRIDGE, Location.ROYAL_PALACE, Location.ARISTOCRAT_PASSAGE, Location.HANGING_TERRACE, Location.CLIFFSIDE_VEINS,
            Location.RAMPARTS_QUARRY, Location.DIVANS_GATEWAY, Location.FAR_RESERVE, Location.GOBLIN_BURROWS, Location.UPPER_MINES,
            Location.ROYAL_MINES, Location.MINERS_GUILD, Location.GREAT_ICE_WALL, Location.THE_MIST, Location.CC_MINECARTS_CO,
            Location.GRAND_LIBRARY, Location.HANGING_COURT);

    /**
     * List of Crystal Hollows zones
     */
    private static final EnumSet<Location> hollowsLocations = EnumSet.of(Location.MAGMA_FIELDS,
           Location.CRYSTAL_HOLLOWS, Location.CRYSTAL_NUCLEUS, Location.JUNGLE, Location.MITHRIL_DEPOSITS, Location.GOBLIN_HOLDOUT,
           Location.PRECURSOR_REMNANT, Location.FAIRY_GROTTO, Location.KHAZAD_DUM, Location.JUNGLE_TEMPLE, Location.MINES_OF_DIVAN,
           Location.GOBLIN_QUEEN_DEN, Location.LOST_PRECURSOR_CITY);

    /**
     * List of Mushroom Desert zones
     */
    private static final EnumSet<Location> mushroomDesertLocations = EnumSet.of(Location.MUSHROOM_DESERT, Location.TRAPPERS_DEN,
            Location.DESERT_SETTLEMENT, Location.GLOWING_MUSHROOM_CAVE, Location.MUSHROOM_GORGE, Location.OVERGROWN_MUSHROOM_CAVE,
            Location.DESERT_MOUNTAIN, Location.SHEPHERDS_KEEP, Location.OASIS, Location.JAKES_HOUSE, Location.TREASURE_HUNTER_CAMP);

    /**
     * List of the Spiders Den zones
     */
    private static final EnumSet<Location> spidersDenLocations = EnumSet.of(Location.SPIDERS_DEN, Location.SPIDER_MOUND,
            Location.ARACHNES_SANCTUARY, Location.ARACHNES_BURROW, Location.GRANDMAS_HOUSE, Location.ARCHAEOLOGISTS_CAMP, Location.GRAVEL_MINES);

    /**
     * List of The End zones
     */
    private static final EnumSet<Location> theEndLocations = EnumSet.of(Location.THE_END, Location.DRAGONS_NEST,
            Location.VOID_SEPULTURE, Location.ZEALOT_BRUISER_HIDEOUT, Location.VOID_SLATE);

    /**
     * List of Winter Island zones
     */
    private static final EnumSet<Location> winterIslandLocations = EnumSet.of(Location.JERRYS_WORKSHOP, Location.JERRY_POND,
            Location. MOUNT_JERRY, Location. GARYS_SHACK, Location.GLACIAL_CAVE, Location.TERRYS_SHACK, Location.HOT_SPRINGS,
            Location.REFLECTIVE_POND, Location.SUNKEN_JERRY_POND, Location.SHERRYS_SHOWROOM, Location. EINARYS_EMPORIUM);

    /**
     * List of locations that spawn zealots/zealot variants
     */
    private static final EnumSet<Location> zealotSpawnLocations = EnumSet.of(Location.DRAGONS_NEST, Location.ZEALOT_BRUISER_HIDEOUT);

    /**
     * List of Rift Dimension zones
     */
    @Getter
    private static final EnumSet<Location> riftLocations = EnumSet.of(Location.WIZARD_TOWER, Location.FAIRYLOSOPHER_TOWER,
            Location.ENIGMAS_CRIB, Location.BROKEN_CAGE, Location.SHIFTED_TAVERN, Location.PUMPGROTTO, Location.THE_BASTION,
            Location.OTHERSIDE, Location.BLACK_LAGOON, Location.LAGOON_CAVE, Location.LAGOON_HUT, Location.LEECHES_LAIR,
            Location.AROUND_COLOSSEUM, Location.RIFT_GALLERY_ENTRANCE, Location.RIFT_GALLERY, Location.WEST_VILLAGE,
            Location.DOLPHIN_TRAINER, Location.CAKE_HOUSE, Location.INFESTED_HOUSE, Location.MIRRORVERSE, Location.DREADFARM,
            Location.GREAT_BEANSTALK, Location.VILLAGE_PLAZA, Location.TAYLORS, Location.LONELY_TERRACE, Location.MURDER_HOUSE,
            Location.BOOK_IN_A_BOOK, Location.HALF_EATEN_CAVE, Location.YOUR_ISLAND, Location.EMPTY_BANK, Location.BARRY_CENTER,
            Location.BARRY_HQ, Location.DEJA_VU_ALLEY, Location.LIVING_CAVE, Location.LIVING_STILLNESS, Location.COLOSSEUM,
            Location.BARRIER_STREET, Location.PHOTON_PATHWAY, Location.STILLGORE_CHATEAU, Location.OUBLIETTE, Location.WYLD_WOODS);

    /**
     * List of Skyblock zones where we might see items in showcases from outside. We don't outline showcase blocks
     * while the player is in this area to avoid disturbing players.
     */
    @Getter
    private static final EnumSet<Location> showcaseLocations = EnumSet.of(Location.VILLAGE, Location.AUCTION_HOUSE,
            Location.BAZAAR, Location.LIBRARY, Location.JERRYS_WORKSHOP);

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

    /**
     * @param slayerQuest slayer type
     * @param location current location
     * @return true if it is the location where the given slayer type is counted
     */
    public static boolean isSlayerLocation(EnumUtils.SlayerQuest slayerQuest, Location location) {
        switch (slayerQuest) {
            case REVENANT_HORROR:
                return location == Location.GRAVEYARD || location == Location.COAL_MINE;
            case TARANTULA_BROODFATHER:
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if (player == null) return false;
                double x = player.prevPosX;
                double y = player.prevPosY;
                double z = player.prevPosZ;
                return location == Location.SPIDER_MOUND || location == Location.ARACHNES_BURROW ||
                        location == Location.ARACHNES_SANCTUARY ||
                        ((location == Location.BURNING_DESERT || location == Location.CRIMSON_ISLE || location == Location.DRAGONTAIL)
                        && (-550 < x && x <-450 && 80 < y && y < 130 && -900 < z && z < -625));
            case SVEN_PACKMASTER:
                return location == Location.RUINS || location == Location.HOWLING_CAVE;
            case VOIDGLOOM_SERAPH:
                return location != Location.VOID_SLATE && isInTheEnd(location);
            case INFERNO_DEMONLORD:
                return location == Location.CRIMSON_ISLE || location == Location.STRONGHOLD ||
                        location == Location.SMOLDERING_TOMB || location == Location.THE_WASTELAND;
            case RIFTSTALKER_BLOODFIEND:
                return location == Location.OUBLIETTE || location == Location.STILLGORE_CHATEAU;
            default:
                return false;
        }
    }
}
