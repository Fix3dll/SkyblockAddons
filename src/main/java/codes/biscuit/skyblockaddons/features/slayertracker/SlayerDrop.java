package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Rarity;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

@Getter
public enum SlayerDrop {

    // Revenant Drops
    REVENANT_FLESH(Items.rotten_flesh, "Revenant Flesh", "REVENANT_FLESH", Rarity.UNCOMMON, true),
    FOUL_FLESH(Items.coal, 1, "Foul Flesh", "FOUL_FLESH", Rarity.RARE),
    PESTILENCE_RUNE("Pestilence Rune", "RUNE", "ZOMBIE_SLAYER", Rarity.RARE, "2bd666fc-150f-3082-baa5-ca9607110dff", "a8c4811395fbf7f620f05cc3175cef1515aaf775ba04a01045027f0693a90147"),
    UNDEAD_CATALYST("Undead Catalyst", "UNDEAD_CATALYST", Rarity.RARE, "aff9a310-b5cf-389b-bead-ad62465a50a8", "55b871506f5a295026579634ee51755b29abe2907cb4248ea5cc36408fce6928"),
    SMITE_SIX("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "smite", 6),
    BEHEADED_HORROR("Beheaded Horror", "BEHEADED_HORROR", Rarity.EPIC, "fa0b3e72-458d-3ccf-97ae-21106c97f424", "dbad99ed3c820b7978190ad08a934a68dfa90d9986825da1c97f6f21f49ad626"),
    REVENANT_CATALYST("Revenant Catalyst", "REVENANT_CATALYST", Rarity.EPIC, "8432539c-2d55-361b-b2c8-4a9603141a9e", "b88cfafa5f03f8aef042a143799e964342df76b7c1eb461f618e398f84a99a63"),
    SNAKE_RUNE("Snake Rune", "RUNE", "SNAKE", Rarity.LEGENDARY, "d9f2b759-001b-340f-ac5b-cb7c9540986b", "2c4a65c689b2d36409100a60c2ab8d3d0a67ce94eea3c1f7ac974fd893568b5d"),
    SCYTHE_BLADE(Items.diamond, "Scythe Blade", "SCYTHE_BLADE", Rarity.LEGENDARY, true),
    SMITE_SEVEN("Enchanted Book", "ENCHANTED_BOOK", Rarity.EPIC, "smite", 7),
    REVENANT_VISCERA(Items.cooked_porkchop, "Revenant Viscera", "REVENANT_VISCERA", Rarity.RARE, true),
    SHARD_OF_SHREDDED("Shard of the Shredded", "SHARD_OF_THE_SHREDDED", Rarity.LEGENDARY, "1efc166e-b195-3271-a1b1-43151eef9b75", "b10857e872711e7e6925c9ef1115353b47f58ef07eb5c2d565f1f86023e7a284"),
    WARDEN_HEART("Warden Heart", "WARDEN_HEART", Rarity.LEGENDARY, "eef9b6d7-a195-3d8a-a216-0e87a585cd83", "19a3b5fab01dfabf1f8a814440a8ce80de109b135a51aef225c9ee98fe447e0"),

    // Tarantula Drops
    TARANTULA_WEB(Items.string, "Tarantula Web", "TARANTULA_WEB", Rarity.UNCOMMON, true),
    TOXIC_ARROW_POISON(Items.dye, 10, "Toxic Arrow Poison", "TOXIC_ARROW_POISON", Rarity.RARE),
    SPIDER_CATALYST("Spider Catalyst", "SPIDER_CATALYST", Rarity.RARE, "d88c6ff7-1185-3e93-bffd-fce06348b05f", "4ceb0ed8fc2272b3d3d820676d52a38e7b2e8da8c687a233e0dabaa16c0e96df"),
    BANE_OF_ARTHROPODS_SIX("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "bane_of_arthropods", 6),
    BITE_RUNE("Bite Rune", "RUNE", "BITE", Rarity.EPIC, "63241d68-39bf-3c00-9903-f058232a5c57", "43a1ad4fcc42fb63c681328e42d63c83ca193b333af2a426728a25a8cc600692"),
    FLY_SWATTER(Items.golden_shovel, "Fly Swatter", "FLY_SWATTER", Rarity.EPIC, true),
    TARANTULA_TALISMAN("Tarantula Talisman", "TARANTULA_TALISMAN", Rarity.EPIC, "b64e0ca0-b8f2-39c0-afa5-6afa22919968", "2f0f123f10c9a19665d5561792d2c9aea067418a0f92dc20490dade70ed38cb1"),
    DIGESTED_MOSQUITO(Items.rotten_flesh, "Digested Mosquito", "DIGESTED_MOSQUITO", Rarity.LEGENDARY),

    // Sven Drops
    WOLF_TOOTH(Items.ghast_tear, "Wolf Tooth", "WOLF_TOOTH", Rarity.UNCOMMON, true),
    HAMSTER_WHEEL(Item.getItemFromBlock(Blocks.trapdoor), "Hamster Wheel", "HAMSTER_WHEEL", Rarity.RARE, true),
    SPIRIT_RUNE("Spirit Rune", "RUNE", "SPIRIT", Rarity.RARE, "86f714a7-d6e5-3c06-b92e-7062ff5d0ade", "c738b8af8d7ce1a26dc6d40180b3589403e11ef36a66d7c4590037732829542e"),
    FURBALL("Furball", "FURBALL", Rarity.RARE, "b234e187-5150-3d21-abe5-1cc0a9cda094", "b548678e44f9f62d350c24a3e01907c0545bef66810590333e654bd931c187db"),
    CRITICAL_SIX("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "critical", 6),
    RED_CLAW_EGG(Items.spawn_egg, 96, "Red Claw Egg", "RED_CLAW_EGG", Rarity.EPIC),
    COUTURE_RUNE("Couture Rune", "RUNE", "COUTURE", Rarity.LEGENDARY, "d9baa447-8f76-30f9-b8f8-d83e750e6488", "734fb3203233efbae82628bd4fca7348cd071e5b7b52407f1d1d2794e31799ff"),
    OVERFLUX_CAPACITOR(Items.quartz, "Overflux Capacitor", "OVERFLUX_CAPACITOR", Rarity.EPIC),
    GRIZZLY_BAIT(Items.fish, 1, "Grizzly Bait", "GRIZZLY_BAIT", Rarity.RARE),

    // Enderman Drops
    NULL_SPHERE(Items.firework_charge, "Null Sphere", "NULL_SPHERE", Rarity.UNCOMMON, true),
    TWILIGHT_ARROW_POISON(Items.dye, 5, "Twilight Arrow Poison", "TWILIGHT_ARROW_POISON", Rarity.UNCOMMON),
    ENDERSNAKE_RUNE("Endersnake Rune", "RUNE", "ENDERSNAKE", Rarity.LEGENDARY, "761518dd-d3bc-3698-ac79-3687e2a6b2e5", "c3a9acbb7d3d49b1d54d26111104d0da57d8b4ab37885b4bbd240ac71074cad2"),
    SUMMONING_EYE("Summoning Eye", "SUMMONING_EYE", Rarity.EPIC, "af6ce31d-cb20-31d9-925d-795c2acf4982", "d3d48224aff85357c3745951a4d85e924a12c2580dc3ad2854cbcf3c12aea4d7"),
    MANA_STEAL_ONE("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "mana_steal", 1),
    TRANSMISSION_TUNER("Transmission Tuner", "TRANSMISSION_TUNER", Rarity.EPIC, "de04f802-e6ac-3559-8dd0-d6de5c941fb8", "52626178b84dbd394cf2409467210282e521032176db21b281fdc8ae9cbe2b2f"),
    NULL_ATOM(Item.getItemFromBlock(Blocks.wooden_button), "Null Atom", "NULL_ATOM", Rarity.RARE, true),
    HAZMAT_ENDERMAN("Hazmat Enderman", "HAZMAT_ENDERMAN", Rarity.LEGENDARY, "37632a73-fe82-3e68-9476-4da81217bde0", "71e72890a79e500cf13a97d1374c5ac8a4f15a9e0d6885997fc6f2e3c11254c"),
    POCKET_ESPRESSO_MACHINE("Pocket Espresso Machine", "POCKET_ESPRESSO_MACHINE", Rarity.COMMON, "b538fa18-f947-35e9-9e2a-6fdf62b11e6b", "666070ce03a545ee4d263bcf27f36338d249d7cb7a2376f92c1673ae134e04b6"),
    SMARTY_PANTS_ONE("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "smarty_pants", 1),
    END_RUNE("End Rune", "RUNE", "ENDERSNAKE", Rarity.EPIC, "594f822b-5016-3553-bdaa-7f94fdb2bbdd", "3b11fb90db7f57beb435954013b1c7ef776c6bd96cbf3308aa8ebac29591ebbd"),
    HANDY_BLOOD_CHALICE("Handy Blood Chalice", "HANDY_BLOOD_CHALICE", Rarity.COMMON, "5cc67a12-86ae-3dca-bb5f-0ecb1255d011", "431cd7ed4e4bf07c3dfd9ba498708e730e69d807335affabc12d87ff542f6a88"),
    SINFUL_DICE("Sinful Dice", "SINFUL_DICE", Rarity.EPIC, "f31fac90-88be-3ee8-b480-1d517728f3d0", "204e23a85b672889617223fe186abac7979268841221e8fcb84554b5d38609eb"),
    EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER("Exceedingly Rare Ender Artifact Upgrader", "EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER", Rarity.LEGENDARY, "887cf48c-a84b-3726-92d0-4dd0d4401cae", "1259231a946987ea53141789a09496f098d6ecac412a01e0a24c906a99fdbd9a"),
    VOID_CONQUEROR_ENDERMAN_SKIN("Void Conqueror Enderman Skin", "PET_SKIN_ENDERMAN_SLAYER", Rarity.EPIC, "d92f10c8-82cb-3ab2-aceb-b50cd0c198c7", "8fff41e1afc597b14f77b8e44e2a134dabe161a1526ade80e6290f2df331dc11"),
    ETHERWARP_MERGER("Etherwarp Merger", "ETHERWARP_MERGER", Rarity.EPIC, "abc85f15-ed0b-3f52-b18c-078d3007fddc", "3e5314f4919691ccbf807743dae47ae45ac2e3ff08f79eecdd452fe602eff7f6"),
    JUDGEMENT_CORE("Judgement Core", "JUDGEMENT_CORE", Rarity.LEGENDARY, "6e38aa1a-fa96-3f13-a59e-0cf8f5d57b61", "95f3726f6bfbc81f07025f1d6cbec7f8f7fa7abdf3a0a86b8197e092214860ba"),
    ENCHANT_RUNE("Enchant Rune", "RUNE", "ENCHANT", Rarity.LEGENDARY, "ad7f0190-4302-3c06-ace2-8073e9ae9c4d", "59ffacec6ee5a23d9cb24a2fe9dc15b24488f5f71006924560bf12148421ae6d"),
    ENDER_SLAYER_SEVEN("Enchanted Book", "ENCHANTED_BOOK", Rarity.RARE, "ender_slayer", 7),

    // Blaze Drops
    DERELICT_ASHE(Items.gunpowder, "Derelict Ashe", "DERELICT_ASHE", Rarity.UNCOMMON, true),
    LAVATEARS_RUNE("Lavatears Rune I", "RUNE", "LAVATEARS", Rarity.LEGENDARY, "87255f9d-54f6-3ff4-9658-6319fa8981e4", "8c8ccd5f863d82bb097b926bc5f4cca97b19f46e11b3a3a59d001adb89886773"),
    WISPS_ICE_FLAVORED_WATER(Items.potionitem, "Wisp's Ice-Flavored Water I Splash Potion", "POTION", Rarity.COMMON),
    BUNDLE_OF_MAGMA_ARROWS("Bundle of Magma Arrows", "ARROW_BUNDLE_MAGMA", Rarity.EPIC, "77bdaea3-6e3b-3d63-b90a-5c673b483bb3", "f4dfafe7ab8e8695a56d31cc8666705d61ed93b1c51910c22f4a950eb55f04dd"),
    MANA_DISINTEGRATOR("Mana Disintegrator", "MANA_DISINTEGRATOR", Rarity.RARE, "2aee71b4-fc7a-3cd8-aa98-9c58562df0bf", "61565c1c265f50398c0646a9b7d64289522c0b089091ba5a04f1d18baa860d85"),
    SCORCHED_BOOKS("Scorched Books", "SCORCHED_BOOKS", Rarity.MYTHIC, "d15a5bd9-33f1-3336-9679-9d4887b84e9c", "46ee7e906686abd5ec192b079314c45f1fb8171d9e13caa4cf9f63afc2263fd5"),
    KELVIN_INVERTER("Kelvin Inverter", "KELVIN_INVERTER", Rarity.RARE, "21e25472-690f-393e-9daf-91fa81a7351c", "445ba4b10ed76c3f13d46bc99f24b5743e92ddbd6f6716c385c08305fdbc999e"),
    BLAZE_ROD_DISTILLATE("Blaze Rod Distillate", "BLAZE_ROD_DISTILLATE", Rarity.RARE, "03792e5c-3fc2-3e6f-bb65-4b8a7e6d000c", "41e813a3a380e3eb201c24b9661f6edb39bdf42a88989f81a90f771997d6f5cd"),
    GLOWSTONE_DISTILLATE("Glowstone Distillate", "GLOWSTONE_DUST_DISTILLATE", Rarity.RARE, "8e7a556b-6b14-31da-8376-2834a51f6261", "a0d174e370e47275638fe2d6b86fcdfe4496eb45265ad11393689470df5ee2b"),
    MAGMA_CREAM_DISTILLATE("Magma Cream Distillate", "MAGMA_CREAM_DISTILLATE", Rarity.RARE, "4cb3bd2e-808d-32a6-b239-0eb9024d34d0", "6cdf636877f0a1edb35aed4cc1fd0e9c9a04cfb8e8809d45cd3514ae05ad2af0"),
    NETHER_WART_DISTILLATE("Nether Wart Distillate", "NETHER_STALK_DISTILLATE", Rarity.RARE, "4cb3bd2e-808d-32a6-b239-0eb9024d34d0", "6cdf636877f0a1edb35aed4cc1fd0e9c9a04cfb8e8809d45cd3514ae05ad2af0"),
    GABAGOOL_DISTILLATE("Gabagool Distillate", "CRUDE_GABAGOOL_DISTILLATE", Rarity.RARE, "e02b9246-624b-3aeb-bf58-c5fcc3e1dc6d", "afd2333b5ee49ec5e3af4c6f45550bc44f88bfb4ad0ab27baa339a04b94397a2"),
    SCORCHED_POWER_CRYSTAL("Scorched Power Crystal", "SCORCHED_POWER_CRYSTAL", Rarity.LEGENDARY, "a91f970e-bcd6-37cf-b54e-4313c6d5795c", "f099d4e25a569e2d018bd8d7e1a40ad2ccbc9bd1fd2ed48688bcc3f62a96213a"),
    ARCHFIEND_DICE("Archfiend Dice", "ARCHFIEND_DICE", Rarity.EPIC, "ce2aa446-9f96-3f32-b83b-2138689cb96a", "8ff7c410a4a8b4b518b94d21402d2892fcc8fa68c3028417dd4eaa8b7e35c568"),
    FIRE_ASPECT_THREE("Enchanted Book", "ENCHANTED_BOOK", Rarity.COMMON, "fire_aspect", 3),
    FIERY_BURST_RUNE("Fiery Burst Rune I", "RUNE", "FIERY_BURST", Rarity.LEGENDARY, "da27a29a-e803-364a-924f-fc2a5ee7f4d9", "8d620e4e3d3abfed6ad81a58a56bcd085d9e9efc803cabb21fa6c9e3969e2d2e"),
    FLAWED_OPAL_GEMSTONE("❂ Flawed Opal Gemstone", "FLAWED_OPAL_GEM", Rarity.UNCOMMON, "68192a68-e91b-32ea-aa6c-d7469b41485b", "eadc3bcdd7c701b63f8b8b4a96e429316a08388669d9a98c1a98791729b961df"),
    DUPLEX("Enchanted Book", "ENCHANTED_BOOK", Rarity.COMMON, "ultimate_reiterate", 1),
    HIGH_CLASS_ARCHFIEND_DICE("High Class Archfiend Dice", "HIGH_CLASS_ARCHFIEND_DICE", Rarity.LEGENDARY, "6d0f2129-9d9a-37e3-ba54-b76855b1249f", "e10b3076a6c8997fe724c966bae5fbb48105b50dc82166392d954b419aed00f6"),
    WILSON_ENGINEERING_PLANS(Items.paper, "Wilson's Engineering Plans", "WILSON_ENGINEERING_PLANS", Rarity.LEGENDARY, true),
    SUBZERO_INVERTER("Subzero Inverter", "SUBZERO_INVERTER", Rarity.LEGENDARY, "f30dbe39-cb6a-3965-827e-e4b86b2d583e", "98f809909a4d4cde7f2a79d998205e4814bcbb7d4bc602a1d5826224ae021786"),

    //Vampire Slayer
    COVEN_SEAL(Items.nether_wart, "Coven Seal", "COVEN_SEAL", Rarity.UNCOMMON),
    QUANTUM_BOOK_BUNDLE("Quantum Bundle", "ENCHANTED_BOOK_BUNDLE_QUANTUM", Rarity.UNCOMMON, "a1112929-40af-3d97-9743-d9f9fb2b4a20", "e5be22b5d4a875d77df3f7710ff4578ef27939a9684cbdb3d31d973f166849"),
    SOULTWIST_RUNE("Soultwist Rune I", "RUNE", "SOULTWIST", Rarity.EPIC, "0618ee1f-2ca3-3caf-bf63-434febd16825", "f5ffdfbd490fc7310d61a1c4c35a4e0cd2f9fccc1239c6a4bcd7dec05e25ea67"),
    BUBBA_BLISTER("Bubba Blister", "BUBBA_BLISTER", Rarity.LEGENDARY, "f2c85cc3-fd79-3f94-8784-4a379c4f68c1", "6cb5727b986c36e07582148b886102cf8070b0ee3aa3771b2464393f588a832d"),
    FANGTASTIC_CHOCOLATE_CHIP(Items.cookie, "Fang-tastic Chocolate Chip", "CHOCOLATE_CHIP", Rarity.LEGENDARY),
    GUARDIAN_LUCKY_BLOCK("Guardian Lucky Block", "GUARDIAN_LUCKY_BLOCK", Rarity.LEGENDARY, "1fa8d033-8b10-3ca4-af9d-1bc243ddcb9e", "46ef21c8575a306999142b0814f163cb403ec0fa51beca81dd074b02dfe266af"),
    MCGRUBBERS_BURGER("McGrubber's Burger", "MCGRUBBER_BURGER", Rarity.EPIC, "f5622ccb-aebc-36b7-9d39-7cbe0824cb6a", "d33ddb92cb6b3a79280b8bdced8976aeab13a4bffeaef2d46d828bd91dee0f3e"), /* old */
    UNFANGED_VAMPIRE_PART("Unfanged Vampire Part", "UNFANGED_VAMPIRE_PART", Rarity.LEGENDARY, "59cc508f-f9f9-3c7b-893c-32096116901c", "e1883eb5e47ceeed02063fb8ec0f7584717ac8c79521ae32cf0606ebcf740e70"), /* old */
    THE_ONE_BOOK_BUNDLE("The One Bundle", "ENCHANTED_BOOK_BUNDLE_THE_ONE", Rarity.LEGENDARY, "ca704bba-9cb8-3220-bb45-0f0c3f8c93a0", "e5be22b5d4a875d77df3f7710ff4578ef27939a9684cbdb3d31d973f166849"); /* old */

    private final String skyblockID;
    private final Rarity rarity;
    private final ItemStack itemStack;
    private String runeID;

    /**
     * Creates a slayer drop with an item, display name, skyblock id, and item rarity
     */
    SlayerDrop(Item item, String name, String skyblockID, Rarity rarity) {
        this(item, name, skyblockID, rarity, false);
    }

    /**
     * Creates an enchanted book slayer drop with a display name, skyblock id, item rarity,
     * skyblock enchant name, and enchant level.
     */
    SlayerDrop(String name, String skyblockID, Rarity rarity, String enchantID, int enchantLevel) {
        this.itemStack = ItemUtils.createEnchantedBook(name, skyblockID, enchantID, enchantLevel);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a slayer drop with an item, display name, skyblock id, item rarity, and enchanted state
     */
    SlayerDrop(Item item, String name, String skyblockID, Rarity rarity, boolean enchanted) {
        this(item, 0, name, skyblockID, rarity, enchanted);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, and item rarity
     */
    SlayerDrop(Item item, int meta, String name, String skyblockID, Rarity rarity) {
        this(item, meta, name, skyblockID, rarity, false);
    }

    /**
     * Creates a slayer drop with an item, item meta, display name, skyblock id, item rarity, and enchanted state
     */
    SlayerDrop(Item item, int meta, String name, String skyblockID, Rarity rarity, boolean enchanted) {
        this.itemStack = ItemUtils.createItemStack(item, meta, name, skyblockID, enchanted);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a player skull with a display name, skyblock id, item rarity, skull id, and skin texture link
     */
    SlayerDrop(String name, String skyblockID, Rarity rarity, String skullID, String textureURL) {
        this.itemStack = ItemUtils.createSkullItemStack(name, skyblockID, skullID, textureURL);
        this.skyblockID = skyblockID;
        this.rarity = rarity;
    }

    /**
     * Creates a player skull with a display name, skyblock id, rune id, item rarity, skull id, and skin texture link
     */
    SlayerDrop(String name, String skyblockID, String runeID, Rarity rarity, String skullID, String textureURL) {
        this(name, skyblockID, rarity, skullID, textureURL);

        this.runeID = runeID;
    }

    private static final HashMap<String, String> internalItemTranslations = new HashMap<>();

    static {
        internalItemTranslations.put("bossesKilled", "Bosses Killed");

        // revenant
        internalItemTranslations.put("revenantFlesh", "Revenant Flesh");
        internalItemTranslations.put("foulFlesh", "Foul Flesh");
        internalItemTranslations.put("pestilenceRune", "Pestilence Rune");
        internalItemTranslations.put("undeadCatalyst", "Undead Catalyst");
        internalItemTranslations.put("smiteSix", "Smite 6");
        internalItemTranslations.put("beheadedHorror", "Beheaded Horror");
        internalItemTranslations.put("revenantCatalyst", "Revenant Catalyst");
        internalItemTranslations.put("snakeRune", "Snake Rune");
        internalItemTranslations.put("scytheBlade", "Scythe Blade");
        internalItemTranslations.put("revenantViscera", "Revenant Viscera");
        internalItemTranslations.put("smiteSeven", "Smite 7");
        internalItemTranslations.put("shardOfShredded", "Shard of Shredded");
        internalItemTranslations.put("wardenHeart", "Warden Heart");

        // tarantula
        internalItemTranslations.put("tarantulaWeb", "Tarantula Web");
        internalItemTranslations.put("toxicArrowPoison", "Toxic Arrow Poison");
        internalItemTranslations.put("spiderCatalyst", "Spider Catalyst");
        internalItemTranslations.put("baneOfArthropodsSix", "Bane Of Arthropods 6");
        internalItemTranslations.put("biteRune", "Bite Rune");
        internalItemTranslations.put("flySwatter", "Fly Swatter");
        internalItemTranslations.put("tarantulaTalisman", "Tarantula Talisman");
        internalItemTranslations.put("digestedMosquito", "Digested Mosquito");

        // wolf
        internalItemTranslations.put("wolfTooth", "Wolf Tooth");
        internalItemTranslations.put("hamsterWheel", "Hamster Wheel");
        internalItemTranslations.put("spiritRune", "Spirit Rune");
        internalItemTranslations.put("criticalSix", "Critical 6");
        internalItemTranslations.put("furball", "Furball");
        internalItemTranslations.put("redClawEgg", "Red Claw Egg");
        internalItemTranslations.put("coutureRune", "Couture Rune");
        internalItemTranslations.put("grizzlyBait", "Grizzly Bait");
        internalItemTranslations.put("overfluxCapacitor", "Overflux Capacitor");

        // voidgloom
        internalItemTranslations.put("nullSphere", "Null Sphere");
        internalItemTranslations.put("twilightArrowPoison", "Twilight Arrow Poison");
        internalItemTranslations.put("endersnakeRune", "Endersnake Rune");
        internalItemTranslations.put("summoningEye", "Summoning Eye");
        internalItemTranslations.put("manaStealOne", "Mana Steal 1");
        internalItemTranslations.put("transmissionTuner", "Transmission Tuner");
        internalItemTranslations.put("nullAtom", "Null Atom");
        internalItemTranslations.put("hazmatEnderman", "Hazmat Enderman");
        internalItemTranslations.put("pocketEspressoMachine", "Pocket Espresso Machine");
        internalItemTranslations.put("smartyPantsOne", "Smarty Pants 1");
        internalItemTranslations.put("endRune", "End Rune");
        internalItemTranslations.put("handyBloodChalice", "Handy Blood Chalice");
        internalItemTranslations.put("sinfulDice", "Sinful Dice");
        internalItemTranslations.put("exceedinglyRareEnderArtifactUpgrader", "Exceedingly Rare Ender Artifact Upgrader");
        internalItemTranslations.put("voidConquerorEndermanSkin", "Void Conqueror Enderman Skin");
        internalItemTranslations.put("etherwarpMerger", "Etherwarp Merger");
        internalItemTranslations.put("judgementCore", "Judgement Core");
        internalItemTranslations.put("enchantRune", "Enchant Rune");
        internalItemTranslations.put("enderSlayerSeven", "Ender Slayer 7");

        // inferno
        internalItemTranslations.put("derelictAshe", "Derelict Ashe");
        internalItemTranslations.put("lavatearsRune", "Lavatears Rune");
        internalItemTranslations.put("wispsIceFlavoredWater", "Wisp's Ice-Flavored Water");
        internalItemTranslations.put("bundleOfMagmaArrows", "Bundle of Magma Arrows");
        internalItemTranslations.put("manaDisintegrator", "Mana Disintegrator");
        internalItemTranslations.put("scorchedBooks", "Scorched Books");
        internalItemTranslations.put("kelvinInverter", "Kelvin Inverter");
        internalItemTranslations.put("blazeRodDistillate", "Blaze Rod Distillate");
        internalItemTranslations.put("glowstoneDistillate", "Glowstone Distillate");
        internalItemTranslations.put("magmaCreamDistillate", "Magma Cream Distillate");
        internalItemTranslations.put("netherWartDistillate", "Nether Wart Distillate");
        internalItemTranslations.put("gabagoolDistillate", "Gabagool Distillate");
        internalItemTranslations.put("scorchedPowerCrystal", "Scorched Power Crystal");
        internalItemTranslations.put("archfiendDice", "Archfiend Dice");
        internalItemTranslations.put("fireAspectThree", "Fire Aspect 3");
        internalItemTranslations.put("fieryBurstRune", "Fiery Burst Rune");
        internalItemTranslations.put("flawedOpalGemstone", "Flawed Opal Gemstone");
        internalItemTranslations.put("duplex", "Duplex 1");
        internalItemTranslations.put("highClassArchfiendDice", "High Class Archfiend Dice");
        internalItemTranslations.put("wilsonEngineeringPlans", "Wilson's Engineering Plans");
        internalItemTranslations.put("subzeroInverter", "Subzero Inverter");

        // rift
        internalItemTranslations.put("covenSeal", "Coven Seal");
        internalItemTranslations.put("quantumBookBundle", "Quantum (Book Bundle)");
        internalItemTranslations.put("soultwistRune", "Soultwist Rune");
        internalItemTranslations.put("bubbaBlister", "Bubba Blister");
        internalItemTranslations.put("fangtasticChocolateChip", "Fang-Tastic Chocolate Chip");
        internalItemTranslations.put("guardianLuckyBlock", "Guardian Lucky Block");
        internalItemTranslations.put("mcgrubbersBurger", "McGrubber's Burger");
        internalItemTranslations.put("unfangedVampirePart", "Unfanged Vampire Part");
        internalItemTranslations.put("theOneBookBundle", "The One (Book Bundle)");
    }

    public String getDisplayName() {
        return internalItemTranslations.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}
