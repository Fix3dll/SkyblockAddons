package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of bait in the Player's Inventory.
 * @author Charzard4261
 */
public class BaitManager {

    /** The BaitListManager instance. */
    @Getter private static final BaitManager instance = new BaitManager();

    public static final Map<BaitType, Integer> DUMMY_BAITS = new HashMap<>();

    static {
        DUMMY_BAITS.put(BaitType.CARROT, 1);
        DUMMY_BAITS.put(BaitType.MINNOW, 2);
        DUMMY_BAITS.put(BaitType.WHALE, 3);
    }

    /**
     * A map of all baits in the inventory and their count
     */
    @Getter private final Map<BaitType, Integer> baitsInInventory = new HashMap<>();

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits() {
        baitsInInventory.clear();

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        for (ItemStack itemStack : player.inventory.mainInventory) {
            BaitType bait;

            // Glowy Chum and Hot bait doesn't contain skyblock id
            String skyblockID = ItemUtils.getSkyblockItemID(itemStack);
            if (skyblockID != null) {
                bait = BaitType.getByItemID(skyblockID);
            } else {
                String skullOwnerID = ItemUtils.getSkullOwnerID(itemStack);

                if (skullOwnerID != null) bait = BaitType.getBySkullID(skullOwnerID);
                else continue;
            }

            if (bait == null)
                continue;

            baitsInInventory.put(bait, baitsInInventory.getOrDefault(bait, 0) + itemStack.stackSize);
        }

    }

    @Getter
    public enum BaitType {
        MINNOW("§fMinnow Bait", "MINNOW_BAIT", "5d93ec7a-9d97-3011-94b7-f60dd937acea", "b8e749ba141c054bb0c125320b17677bdcb3a7e9807a57527c0877b94548a2e"),
        FISH("§fFish Bait", "FISH_BAIT", "2827b15e-7f78-3421-a803-fdee75521ef3", "49e8dc9fdc9f00e1d17666a4787d34cca7cc6dc030f0dc686195546f81c6f22"),
        LIGHT("§fLight Bait", "LIGHT_BAIT", "279190f0-3a97-33c7-a436-27946524e87c", "e3bcc16f402342b4acd29aecac72d1d5a116e0abb478bb4960e734fd70686ba"),
        DARK("§fDark Bait", "DARK_BAIT", "37e39a45-fc4e-31f2-bab4-df51031bf96d", "7281618a5c8239078fd43fef8eae14ea00665f635bd56d4451cccd5d0fa11821"),
        SPIKED("§fSpiked Bait", "SPIKED_BAIT", "f797122d-4f47-37ff-96d6-90803a113a87", "e42c436c6580fad217323b9f045daa23a9f88219ac26506637669eff20901f74"),
        SPOOKY("§fSpooky Bait", "SPOOKY_BAIT", "07719865-57c3-3583-b203-c09c89ae278f", "977eab558e6a90f7cedd72c2416c891f47d71eb4e7bdcc299a6335286f5cba98"),
        CARROT("§fCarrot Bait", "CARROT_BAIT", "0842969c-2692-3bd7-8483-2b7b2c2b7f63", "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"),
        BLESSED("§aBlessed Bait", "BLESSED_BAIT", "28053a44-2919-3e41-b050-c14d8869c3a9", "c2b910b5897cdb86b21ebfcba544afa470ae6d228bee3494427c9c7e8f33502b"),
        WHALE("§9Whale Bait", "WHALE_BAIT", "d29b8346-e083-3e5f-b7a3-215f059ac614", "5b4fb49ae77cfcf0b8f62df9c69cc96b27096ad2737d6d0aa450b50664fb2303"),
        ICE("§aIce Bait", "ICE_BAIT", "26436699-a4b7-3135-bc6a-85d1195675ca", "609e161bdc325c71572a548a79bb15481c924d63e4fb821379d5dd6c8929f39f"),
        SHARK("§aShark Bait", "SHARK_BAIT", "34f3da30-d258-3416-a743-b3594ab044ec", "edff904124efe486b3a54261dbb8072b0a4e11615ad8d7394d814e0e8c8ef9eb"),
        CORRUPTED("§fCorrupted Bait", "CORRUPTED_BAIT", "e4785c0e-3c90-3af3-9ac8-c8c49653af4f", "4bbcddd45cd347865bceab3e3dc5d382723463963f85ecce81cdd61b53db14e4"),
        GLOWY_CHUM("§aGlowy Chum Bait", "GLOWY_CHUM_BAIT", "aed1035d-0ce1-35e6-b532-8c26a596f510", "dfdc1eed684dd805eae96d132e3da53d64267d7361388d5e2c67f5969871e71d"),
        HOT("§aHot Bait", "HOT_BAIT", "e1eada20-e1b5-39ee-8dcb-b527054e33e3", "213c6899d97109c6cacbbcdd01e8900abaf46432f197595baa15ad137d5fb9ba"),
        OBFUSCATED_FISH_1_BRONZE("§f§kObfuscated 1 §8§LBRONZE", "OBFUSCATED_FISH_1_BRONZE", "6044c06f-fb6e-3de8-9e7f-485b0edfdf3a", "e1f4d91e1bf8d3c4258fe0f28ec2fa40670e25ba06ac4b5cb1abf52a83731a9c"),
        OBFUSCATED_FISH_1_SILVER("§f§kObfuscated 1 §7§LSILVER", "OBFUSCATED_FISH_1_SILVER", "e6a123b1-fcf0-32e9-bad2-c1d062d3638f", "479b52391ff0cd3c83db1c1c218a02ab13a2c6a4aaa1cf126c7912bd377e8fbf"),
        OBFUSCATED_FISH_1_GOLD("§f§kObfuscated 1 §6§LGOLD", "OBFUSCATED_FISH_1_GOLD", "c62249e8-87d6-3a94-a94c-d6fd8f4d89af", "8a2a44913ee1d5babc172f374351ea7ad1516ca256d16a4ef72d8a092b519cd1"),
        OBFUSCATED_FISH_1_DIAMOND("§f§kObfuscated 1 §B§LDIAMOND", "OBFUSCATED_FISH_1_DIAMOND", "80d91bcf-4a5f-3c60-a054-6313ba96a5dc", "caa0b4b4f443257e83176df4ffd550de7ee89867e506b9c1ca53f33611327929"),
        OBFUSCATED_FISH_2_BRONZE("§a§kObfuscated 2 §8§LBRONZE", "OBFUSCATED_FISH_2_BRONZE", "6bc494d4-91a5-3bf6-9d2c-e527232a4d34", "8321e19aa4b3163c8990b066b1cd0895c3c97a799057327507db0ffc80d90575"),
        OBFUSCATED_FISH_2_SILVER("§a§kObfuscated 2 §7§LSILVER", "OBFUSCATED_FISH_2_SILVER", "684f2c41-ef26-31fb-96ec-dbbc3b3be6fa", "cb12de47e0b48ab8f7d8f500fdc5d7869b7f2192f823620088582a56afcf68fb"),
        OBFUSCATED_FISH_2_GOLD("§a§kObfuscated 2 §6§LGOLD", "OBFUSCATED_FISH_2_GOLD", "53807fee-2e39-3230-bdd1-3efcf0be73a9", "4631953a0351988029b90e838181e4e563d782e470ea33b8c612756f730625c2"),
        OBFUSCATED_FISH_2_DIAMOND("§a§kObfuscated 2 §B§LDIAMOND", "OBFUSCATED_FISH_2_DIAMOND", "a8e16045-e364-3050-ab40-cde339c36c7e", "4055f82614fc812b611950e3ad2e6921a60f1a275565a7dea2c0fc22a65827f0"),
        FROZEN("§9Frozen Bait", "FROZEN_BAIT", "66d603f6-32bd-3a51-9193-6491f75bd7ae", "38dc68a97cefe92c8cdaa7cb1a7a4de8f16c161da736edf54f79b74beecd6513"),
        WORM("§aWorm Bait", "WORM_BAIT", "730a6086-ad87-38fa-8fa4-0b76a060f4fc", "df03ad96092f3f789902436709cdf69de6b727c121b3c2daef9ffa1ccaed186c"),
        GOLDEN("§aGolden Bait", "GOLDEN_BAIT", "341b5583-eb74-35f3-af85-eee6bbce133a", "72e2908dbb112dcd0b367df43fafcc41a56d9cf803e90a367834b4911f84f391"),
        HOTSPOT("§9Hotspot Bait", "HOTSPOT_BAIT", "5e1d8996-7d4f-3756-98f4-a78f388d652e", "de9b17db5c4cadef737e2fefb42a0123c32cbeaa1ca8932579eb2f05018612cd"),
        TREASURE("§9Treasure Bait", "TREASURE_BAIT", "09266805-e22b-3608-ad37-9a0d35daec14", "c1695c80854447b5db5a0ee6d57ef0a7d91d815bd7e6318c516a39d12fe0639e")
        ;

        private final String itemID;
        private final String skullID;
        private final ItemStack itemStack;

        BaitType(String name, String itemID, String skullID, String textureURL) {
            this.itemID = itemID;
            this.skullID = skullID;
            this.itemStack = ItemUtils.createSkullItemStack(name, itemID, skullID, textureURL);
        }

        /**
         * Check to see if the given name matches a bait's skyblock item ID
         * @return The matching BaitType or null
         */
        public static BaitType getByItemID(String itemID) {
            for (BaitType bait : values()) {
                if (itemID.startsWith(bait.itemID)) {
                    return bait;
                }
            }
            return null;
        }

        /**
         * Check to see if the given id matches a bait's skull owner ID
         * @return The matching BaitType or null
         */
        public static BaitType getBySkullID(String skullID) {
            for (BaitType bait : values()) {
                if (skullID.equals(bait.skullID)) {
                    return bait;
                }
            }
            return null;
        }
    }
}