package codes.biscuit.skyblockaddons.features.dungeon;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Getter
public enum DungeonClass {
    HEALER(Items.potionitem, "Healer", ColorCode.LIGHT_PURPLE),
    ARCHER(Items.bow, "Archer", ColorCode.GOLD),
    TANK(Items.leather_chestplate, "Tank", ColorCode.DARK_GREEN),
    MAGE(Items.blaze_rod, "Mage", ColorCode.AQUA),
    BERSERK(Items.iron_sword, "Berserk", ColorCode.DARK_RED);

    private final char firstLetter;
    private final ItemStack item;
    private final String chatDisplayName; // The way Hypixel writes it out in chat
    private final ColorCode color;

    DungeonClass(Item item, String chatDisplayName, ColorCode color) {
        this.firstLetter = this.name().charAt(0);
        this.item = new ItemStack(item);
        this.chatDisplayName = chatDisplayName;
        this.color = color;
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
