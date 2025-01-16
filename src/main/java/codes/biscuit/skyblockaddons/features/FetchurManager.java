package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.time.Instant;

/**
 * Manages the Fetchur Feature, Pointing out which item Fetchur wants next
 * @author Pedro9558
 */
public class FetchurManager {

    @Getter
    private static final FetchurManager instance = new FetchurManager();
    private static final long MILLISECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;

    @Getter
    private final String fetchurTaskCompletedPhrase = "thanks thats probably what i needed";

    @Getter
    private final String fetchurAlreadyDidTaskPhrase = "come back another time, maybe tmrw";

    /**
     * A list containing the items Fetchur wants.
     * Changing the order will affect the algorithm
     */
    private static final FetchurItem[] items = new FetchurItem[]{
            new FetchurItem(new ItemStack(Blocks.stained_glass, 20, 4), "Yellow Stained Glass"),
            new FetchurItem(new ItemStack(Items.compass, 1), "Compass"),
            new FetchurItem(new ItemStack(Items.prismarine_crystals, 20), "Mithril"),
            new FetchurItem(new ItemStack(Items.fireworks, 1), "Firework Rocket"),
            new FetchurItem(ItemUtils.createSkullItemStack("§fCheap Coffee", null, "2fd02c32-6d35-3a1a-958b-e8c5a657c7d4", "194221a0de936bac5ce895f2acad19c64795c18ce5555b971594205bd3ec"), "Cheap Coffee"),
            new FetchurItem(new ItemStack(Items.oak_door, 1), "Wooden Door"),
            new FetchurItem(new ItemStack(Items.rabbit_foot, 3), "Rabbit's Feet"),
            new FetchurItem(new ItemStack(Blocks.tnt, 1), "Superboom TNT"),
            new FetchurItem(new ItemStack(Blocks.pumpkin, 1), "Pumpkin"),
            new FetchurItem(new ItemStack(Items.flint_and_steel, 1), "Flint and Steel"),
            new FetchurItem(new ItemStack(Items.emerald, 50), "Emerald"),
            //new FetchurItem(new ItemStack(Items.ender_pearl, 16), "Ender Pearl"),
            new FetchurItem(new ItemStack(Blocks.wool, 50, 14), "Red Wool")
    };

    // Used for storage, essential for Fetchur Warner
    @Getter @Setter
    private FetchurItem currentItemSaved = null;

    /**
     * Get the item fetchur needs today
     *
     * @return the item
     */
    public FetchurItem getCurrentFetchurItem() {
        // Get the zero-based day of the month
        int dayIdx = getFetchurDayOfMonth(System.currentTimeMillis()) - 1;
        return items[dayIdx % items.length];
    }

    /**
     * Figure out whether the player submitted today's Fetchur item.
     * Can return incorrect answer if the player handed in Fetchur today, but SBA wasn't loaded at the time.
     * Clicking Fetchur again (and reading the NPC response) will update the value to be correct.
     * @return {@code true} iff the player hasn't yet submitted the item in today (EST).
     */
    public boolean hasFetchedToday() {
        long lastTimeFetched = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getLastTimeFetchur();
        long currTime = System.currentTimeMillis();
        // Return true if the days of the month from last submission and current time match
        return currTime - lastTimeFetched < MILLISECONDS_IN_A_DAY && getFetchurDayOfMonth(lastTimeFetched) == getFetchurDayOfMonth(currTime);
    }

    /**
     * Returns the day of the month in the Fetchur calendar (EST time zone)
     * @param currTimeMillis Epoch UTC milliseconds (e.g. from {@link System#currentTimeMillis()})
     * @return the 1-indexed day of the month in the Fetchur time zone
     */
    private int getFetchurDayOfMonth(long currTimeMillis) {
        return Instant.ofEpochMilli(currTimeMillis).atZone(SkyblockAddons.getHypixelZoneId()).getDayOfMonth();
    }

    /**
     * Called periodically to check for any changes in the Fetchur item.
     * Will also notify the player of a change if enabled.
     */
    public void recalculateFetchurItem() {
        FetchurItem item = getCurrentFetchurItem();
        if (!item.equals(currentItemSaved)) {
            currentItemSaved = item;
            SkyblockAddons main = SkyblockAddons.getInstance();
            // Warn player when there's a change
            if (Feature.FETCHUR_TODAY.isEnabled(FeatureSetting.WARN_WHEN_FETCHUR_CHANGES)) {
                main.getUtils().playLoudSound("random.orb", 0.5);
                main.getRenderListener().setTitleFeature(Feature.FETCHUR_TODAY);
            }
        }
    }

    /**
     * Triggered if the player has just given the correct item, or has already given the correct item, to Fetchur.
     */
    public void saveLastTimeFetched() {
        SkyblockAddons.getInstance().getPersistentValuesManager().setLastTimeFetchur(System.currentTimeMillis());
    }

    /**
     * Called after persistent loading to seed the saved item (so the warning doesn't trigger when joining skyblock)
     */
    public void postPersistentConfigLoad() {
        if (hasFetchedToday()) {
            currentItemSaved = getCurrentFetchurItem();
        }
    }

    /**
     * A class representing the item fetchur wants containing the item instance and the text format of the item
     */
    @Getter
    public static class FetchurItem {
        private final ItemStack itemStack;
        private final String itemText;

        FetchurItem(ItemStack itemStack, String itemText) {
            this.itemStack = itemStack;
            this.itemText = itemText;
        }

        @Override
        public boolean equals(Object anotherObject) {
            if (anotherObject instanceof FetchurItem) {
                FetchurItem another = (FetchurItem) anotherObject;
                return another.itemText.equals(this.itemText)
                        && another.itemStack.getIsItemStackEqual(this.itemStack);
            }
            return false;
        }
    }

}
