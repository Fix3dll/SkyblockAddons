package codes.biscuit.skyblockaddons.core;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Contains all of the Skyblock locations (I hope).
 */
@Getter
public enum Location {
    ISLAND("Your Island"), // TODO RPC
    GUEST_ISLAND("'s Island", "island"), // TODO RPC
    GARDEN("The Garden"),
    GARDEN_PLOT("Plot -"), //TODO RPC

    // https://wiki.hypixel.net/Hub#Zones
    ARCHERY_RANGE("Archery Range"),
    AUCTION_HOUSE("Auction House"),
    BANK("Bank"),
    BAZAAR("Bazaar Alley"), // TODO RPC
    BLACKSMITH("Blacksmith"),
    BULDERS_HOUSE("Builder's House"),
    CANVAS_ROOM("Canvas Room"),
    COAL_MINE("Coal Mine"),
    COLOSSEUM("Colosseum"),
    COMMUNITY_CENTER("Community Center"),
    ELECTION_ROOM("Election Room"),
    FARM("Farm"),
    FARMHOUSE("Farmhouse"),
    FASHION_SHOP("Fashion Shop"),
    FISHERMANS_HUT("Fisherman's Hut"),
    FLOWER_HOUSE("Flower House"),
    FOREST("Forest"),
    GRAVEYARD("Graveyard"),
    CATACOMBS_ENTRANCE("Catacombs Entrance"),
    HEXATORUM("Hexatorum"),
    UNINCORPORATED("Unincorporated"),
    LIBRARY("Library"),
    MOUNTAIN("Mountain"),
    MUSEUM("Museum"),
    PET_CARE("Pet Care"),
    RUINS("Ruins"),
    TAVERN("Tavern"),
    THAUMATURGIST("Thaumaturgist"),
    VILLAGE("Village"),
    WEAPONSMITH("Weaponsmith"),
    WILDERNESS("Wilderness"),
    WIZARD_TOWER("Wizard Tower"),

    SHENS_AUCTION("Shen's Auction"),
    REGALIA_ROOM("Regalia Room"),
    DARK_AUCTION("Dark Auction"),

    // https://wiki.hypixel.net/The_Park#Zones
    BIRCH_PARK("Birch Park"),
    HOWLING_CAVE("Howling Cave"),
    SPRUCE_WOODS("Spruce Woods"),
    LONELY_ISLAND("Lonely Island"),
    VIKING_LONGHOUSE("Viking Longhouse"),
    DARK_THICKET("Dark Thicket"),
    SAVANNA_WOODLAND("Savanna Woodland"),
    MELODYS_PLATEAU("Melody's Plateau"),
    JUNGLE_ISLAND("Jungle Island"),

    GOLD_MINE("Gold Mine"),

    // https://wiki.hypixel.net/Deep_Caverns#Zones
    DEEP_CAVERNS("Deep Caverns"), // TODO RPC
    GUNPOWDER_MINES("Gunpowder Mines", "deep-caverns"), // TODO RPC
    LAPIS_QUARRY("Lapis Quarry", "deep-caverns"), // TODO RPC
    PIGMAN_DEN("Pigmen's Den", "deep-caverns"), // TODO RPC
    SLIMEHILL("Slimehill", "deep-caverns"),
    DIAMOND_RESERVE("Diamond Reserve", "deep-caverns"),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary", "deep-caverns"),

    // Mineshaft
    DWARVEN_BASE_CAMP("Dwarven Base Camp"),
    FOSSIL_RESEARCH_CENTER("Fossil Research Center"),
    GLACITE_TUNNELS("Glacite Tunnels"),
    GLACITE_LAKE("Glacite Lake"),
    GLACITE_MINESHAFTS("Glacite Mineshafts"),

    // https://wiki.hypixel.net/Dwarven_Mines#Zones
    DWARVEN_MINES("Dwarven Mines"),
    ARISTOCRAT_PASSAGE("Aristocrat Passage"),
    BARRACKS_OF_HEROES("Barracks Of Heroes"),
    CC_MINECARTS_CO("C&C Minecarts Co."),
    CLIFFSIDE_VEINS("Cliffside Veins"),
    DIVANS_GATEWAY("Divan's Gateway"),
    DWARVEN_TAVERN("Dwarven Tavern", "tavern"),
    DWARVEN_VILLAGE("Dwarven Village"),
    FAR_RESERVE("Far Reserve"),
    FORGE_BASIN("Forge Basin"),
    GATES_TO_THE_MINES("Gates to the Mines"),
    GOBLIN_BURROWS("Goblin Burrows"),
    GRAND_LIBRARY("Grand Library"),
    GREAT_ICE_WALL("Great Ice Wall"),
    HANGING_COURT("Hanging Court"),
    LAVA_SPRINGS("Lava Springs"),
    MINERS_GUILD("Miner's Guild"),
    PALACE_BRIDGE("Palace Bridge"),
    RAMPARTS_QUARRY("Rampart's Quarry"),
    ROYAL_MINES("Royal Mines"),
    ROYAL_PALACE("Royal Palace"),
    ROYAL_QUARTERS("Royal Quarters"),
    THE_FORGE("The Forge"),
    THE_LIFT("The Lift"),
    THE_MIST("The Mist"),
    UPPER_MINES("Upper Mines"),
    HANGING_TERRACE("Hanging Terrace"), // TODO ?

    // https://wiki.hypixel.net/Crystal_Hollows#Zones
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    CRYSTAL_NUCLEUS("Crystal Nucleus"),
    DRAGONS_LAIR("Dragon's Lair"),
    FAIRY_GROTTO("Fairy Grotto"),
    GOBLIN_HOLDOUT("Goblin Holdout"),
    GOBLIN_QUEEN_DEN("Goblin Queens Den"),
    JUNGLE_TEMPLE("Jungle Temple"),
    JUNGLE("Jungle"),
    KHAZAD_DUM("Khazad-dûm"), //These are the random gen places in each biome
    LOST_PRECURSOR_CITY("Lost Precursor City"),
    MAGMA_FIELDS("Magma Fields"),
    MINES_OF_DIVAN("Mines of Divan"),
    MITHRIL_DEPOSITS("Mithril Deposits"),
    PRECURSOR_REMNANT("Precursor Remnants"),

    THE_BARN("The Barn"),

    // https://wiki.hypixel.net/Mushroom_Desert#Zones
    MUSHROOM_DESERT("Mushroom Desert", "mushroom-desert"),
    DESERT_MOUNTAIN("Desert Mountain", "mushroom-desert"),
    DESERT_SETTLEMENT("Desert Settlement", "mushroom-desert"),
    GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave", "mushroom-desert"),
    JAKES_HOUSE("Jake's House", "mushroom-desert"),
    MUSHROOM_GORGE("Mushroom Gorge", "mushroom-desert"),
    OASIS("Oasis", "mushroom-desert"),
    OVERGROWN_MUSHROOM_CAVE("Overgrown Mushroom Cave", "mushroom-desert"),
    SHEPHERDS_KEEP("Shepherds Keep", "mushroom-desert"),
    TRAPPERS_DEN("Trapper's Den", "mushroom-desert"),
    TREASURE_HUNTER_CAMP("Treasure Hunter Camp", "mushroom-desert"),

    // Spider's Den
    SPIDERS_DEN("Spider's Den"),
    SPIDER_MOUND("Spider Mound", "spiders-den"),
    ARACHNES_BURROW("Arachne's Burrow", "spiders-den"),
    ARACHNES_SANCTUARY("Arachne's Sanctuary", "spiders-den"),
    GRANDMAS_HOUSE("Grandma's House", "spiders-den"),
    ARCHAEOLOGISTS_CAMP("Archaeologist's Camp", "spiders-den"),
    GRAVEL_MINES("Gravel Mines", "spiders-den"),

    // https://wiki.hypixel.net/The_End#Zones
    THE_END("The End"),
    DRAGONS_NEST("Dragon's Nest"),
    VOID_SEPULTURE("Void Sepulture", "the-end"),
    VOID_SLATE("Void Slate", "the-end"),
    ZEALOT_BRUISER_HIDEOUT("Zealot Bruiser Hideout", "the-end"),

    // Jerry's workshop
    JERRY_POND("Jerry Pond"), // TODO RPC
    JERRYS_WORKSHOP("Jerry's Workshop"), // TODO RPC
    MOUNT_JERRY("Mount Jerry"),
    GARYS_SHACK("Gary's Shack"),
    GLACIAL_CAVE("Glacial Cave"),
    TERRYS_SHACK("Terry's Shack"),
    HOT_SPRINGS("Hot Springs"),
    REFLECTIVE_POND("Reflective Pond"),
    SUNKEN_JERRY_POND("Sunken Jerry Pond"),
    SHERRYS_SHOWROOM("Sherry's Showroom"),
    EINARYS_EMPORIUM("Einary's Emporium"),

    // Dungeons
    THE_CATACOMBS("The Catacombs"), // TODO RPC
    DUNGEON_HUB("Dungeon Hub"), // TODO RPC

    // Kuudra Instances
    KUUDRAS_HOLLOW("Kuudra's Hollow"),

    // Crimson Isle
    CRIMSON_ISLE("Crimson Isle"),
    CRIMSON_FIELDS("Crimson Fields"),
    CATHEDRAL("Cathedral"),
    BARBARIAN_OUTPOST("Barbarian Outpost"),
    MAGE_OUTPOST("Mage Outpost"),
    THE_BASTION("The Bastion"),
    BLAZING_VOLCANO("Blazing Volcano"),
    BURNING_DESERT("Burning Desert"),
    DOJO("Dojo"),
    DRAGONTAIL("Dragontail"),
    DRAGONTAIL_TOWNSQUARE("Dragontail Townsquare"),
    DRAGONTAIL_AUCTION_HOUS("Dragontail AH"),
    DRAGONTAIL_BAZAAR("Dragontail BZ"),
    DRAGONTAIL_BANK("Dragontail Bank"),
    DRAGONTAIL_BLACKSMITH("Dragontail Blacksmith"),
    MINION_SHOP("Minion Shop"),
    CHIEFS_HUT("Chief's Hut"),
    FORGOTTEN_SKULL("Forgotten Skull"),
    MAGMA_CHAMBER("Magma Chamber"),
    MYSTIC_MARSH("Mystic Marsh"),
    ODGERS_HUT("Odger's Hut"),
    RUINS_OF_ASHFANG("Ruins of Ashfang"),
    SCARLETON("Scarleton"),
    SCARLETON_PLAZA("Scarleton Plaza"),
    SCARLETON_AUCTION_HOUS("Scarleton AH"),
    SCARLETON_BAZAAR("Scarleton BZ"),
    SCARLETON_BANK("Scarleton Bank"),
    SCARLETON_BLACKSMITH("Scarleton Blacksmith"),
    SCARLETON_MINION_SHOP("Scarleton Minion Shop"),
    STRONGHOLD("Stronghold"),
    THE_WASTELAND("The Wasteland"),
    MATRIARCHS_LAIR("Matriarch's Lair"),
    BELLY_OF_THE_BEAST("Belly of the Beast"),
    AURAS_LAB("Aura's Lab"),
    COURTYARD("Courtyard"),
    IGRUPANS_CHICKEN_COOP("Igrupan's Chicken Coop"),
    THRONE_ROOM("Throne Room"),
    MAGE_COUNCIL("Mage Council"),
    PLHLEGBLAST_POOL("Plhlegblast Pool"),
    SMOLDERING_TOMB("Smoldering Tomb"),
    THE_DUKEDOM("The Dukedom"),

    // https://wiki.hypixel.net/Rift_Dimension#Zones
    /* existing ones
    WIZARD_TOWER("Wizard Tower"),
    THE_BASTION("The Bastion"),
    COLOSSEUM("Colosseum"),*/
    WYLD_WOODS("Wyld Woods"),
    ENIGMAS_CRIB("Enigma's Crib"),
    BROKEN_CAGE("Broken Cage"),
    SHIFTED_TAVERN("Shifted Tavern"),
    PUMPGROTTO("Pumpgrotto"),
    OTHERSIDE("Otherside"),
    BLACK_LAGOON("Black Lagoon"),
    LAGOON_CAVE("Lagoon Cave"),
    LAGOON_HUT("Lagoon Hut"),
    LEECHES_LAIR("Leeches Lair"),
    AROUND_COLOSSEUM("Around Colosseum"),
    RIFT_GALLERY_ENTRANCE("Rift Gallery Entrance"),
    RIFT_GALLERY("Rift Gallery"),
    WEST_VILLAGE("West Village"),
    DOLPHIN_TRAINER("Dolphin Trainer"),
    CAKE_HOUSE("Cake House"),
    INFESTED_HOUSE("Infested House"),
    MIRRORVERSE("Mirrorverse"),
    DREADFARM("Dreadfarm"),
    GREAT_BEANSTALK("Great Beanstalk"),
    VILLAGE_PLAZA("Village Plaza"),
    TAYLORS("Taylor's"),
    LONELY_TERRACE("Lonely Terrace"),
    MURDER_HOUSE("Murder House"),
    BOOK_IN_A_BOOK("Book In A Book"),
    HALF_EATEN_CAVE("Half-Eaten Cave"),
    YOUR_ISLAND("\"Your\" Island"),
    EMPTY_BANK("Empty Bank"),
    BARRY_CENTER("Barry Center"),
    BARRY_HQ("Barry HQ"),
    DEJA_VU_ALLEY("Déjà Vu Alley"),
    LIVING_CAVE("Living Cave"),
    LIVING_STILLNESS("Living Stillness"),
    BARRIER_STREET("Barrier Street"),
    PHOTON_PATHWAY("Photon Pathway"),
    STILLGORE_CHATEAU("Stillgore Château"),
    OUBLIETTE("Oubliette"),
    FAIRYLOSOPHER_TOWER("Fairylosopher Tower"),

    /*
    Out of Bounds
    This is a valid location in Skyblock, it isn't a placeholder or a made up location.
    It actually displays when the player is out of bounds.
    */
    NONE("None"),

    // This is used when the mod is unable to retrieve the player's location from the sidebar.
    UNKNOWN("Unknown"),
    ;

    /**
     * The name of this location as shown on the in-game scoreboard.
     */
    @Setter private String scoreboardName;

    private final String discordIconKey;

    Location(String scoreboardName, String discordIconKey) {
        this.scoreboardName = scoreboardName;
        this.discordIconKey = discordIconKey;
    }

    Location(String scoreboardName) {
        this.scoreboardName = scoreboardName;

        Set<String> NO_DISCORD_RPC = Sets.newHashSet("ISLAND", "BAZAAR", "DEEP_CAVERNS", "GUNPOWDER_MINES", "LAPIS_QUARRY", "PIGMAN_DEN", "JERRYS_WORKSHOP", "JERRY_POND",
                "DWARVEN_MINES", "DWARVEN_VILLAGE", "GATES_TO_THE_MINES", "THE_LIFT", "THE_FORGE", "FORGE_BASIN", "LAVA_SPRINGS", "PALACE_BRIDGE", "ROYAL_PALACE",
                "ARISTOCRAT_PASSAGE", "HANGING_TERRACE", "CLIFFSIDE_VEINS", "RAMPARTS_QUARRY", "DIVANS_GATEWAY", "FAR_RESERVE", "GOBLIN_BURROWs", "UPPER_MINES",
                "MINERS_GUILD", "GREAT_ICE_WALL", "THE_MIST", "CC_MINECARTS_CO", "GRAND_LIBRARY", "HANGING_COURT", "ROYAL_MINES",
                "DESERT_SETTLEMENT", "TREASURE_HUNTER_CAMP", "OASIS", "MUSHROOM_GORGE", "GLOWING_MUSHROOM_CAVE", "OVERGROWN_MUSHROOM_CAVE", "JAKES_HOUSE", "SHEPHERDS_KEEP", "TRAPPERS_DEN",
                "DOJO", "BURNING_DESERT", "BLAZING_VOLCANO", "THE_BASTION", "MAGE_OUTPOST", "BARBARIAN_OUTPOST", "CATHEDRAL", "CRIMSON_FIELDS", "CRIMSON_ISLE", "MINION_SHOP",
                "DRAGONTAIL_BLACKSMITH", "DRAGONTAIL_BANK", "DRAGONTAIL_BAZAAR", "DRAGONTAIL_AUCTION_HOUS", "DRAGONTAIL_TOWNSQUARE", "DRAGONTAIL", "RUINS_OF_ASHFANG", "ODGERS_HUT",
                "MAGMA_CHAMBER", "KUUDRAS_END", "FORGOTTEN_SKULL", "CHIEFS_HUT", "STRONGHOLD", "SCARLETON_MINION_SHOP", "SCARLETON_BLACKSMITH", "SCARLETON_BANK", "SCARLETON_BAZAAR",
                "SCARLETON_PLAZA", "SCARLETON", "PLHLEGBLAST_POOL", "MAGE_COUNCIL", "THRONE_ROOM", "IGRUPANS_CHICKEN_COOP", "COURTYARD", "AURAS_LAB", "BELLY_OF_THE_BEAST", "MATRIARCHS_LAIR",
                "SMOLDERING_TOMB", "MYSTIC_MARSH", "SCARLETON_AUCTION_HOUS", "THE_WASTELAND");

        if (NO_DISCORD_RPC.contains(name())) {
            discordIconKey = "skyblock";
        } else {
            discordIconKey = name().toLowerCase().replace("_", "-");
        }
    }
}
