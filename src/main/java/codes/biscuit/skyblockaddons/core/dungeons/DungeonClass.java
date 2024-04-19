package codes.biscuit.skyblockaddons.core.dungeons;

import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Getter
public enum DungeonClass {

    HEALER(Items.potionitem, "Healer"),
    ARCHER(Items.bow, "Archer"),
    TANK(Items.leather_chestplate, "Tank"),
    MAGE(Items.blaze_rod, "Mage"),
    BERSERK(Items.iron_sword, "Berserk");

    private final char firstLetter;
    private final ItemStack item;
    private final String chatDisplayName; // The way Hypixel writes it out in chat

    DungeonClass(Item item, String chatDisplayName) {
        this.firstLetter = this.name().charAt(0);
        this.item = new ItemStack(item);
        this.chatDisplayName = chatDisplayName;
    }

    public static DungeonClass fromFirstLetter(char firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter == firstLetter) {
                return dungeonClass;
            }
        }
        return null;
    }

    public static DungeonClass fromDisplayName(String name) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.getChatDisplayName().equals(name)) {
                return dungeonClass;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DungeonClass{" +
                "chatDisplayName='" + chatDisplayName + '\'' +
                '}';
    }
}
