package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.time.Instant;

/**
 * Manages the Fetchur Feature, Pointing out which item Fetchur wants next
 */
public class FetchurManager {

    @Getter
    private static final FetchurManager instance = new FetchurManager();
    private static final long MILLISECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;

    @Getter private final String fetchurTaskCompletedPhrase = "thanks thats probably what i needed";
    @Getter private final String fetchurAlreadyDidTaskPhrase = "come back another time, maybe tmrw";

    /**
     * A list containing the items Fetchur wants.
     * Changing the order will affect the algorithm
     */
    private static final FetchurItem[] items = new FetchurItem[] {
            new FetchurItem(Items.YELLOW_STAINED_GLASS, 20, "Yellow Stained Glass"),
            new FetchurItem(Items.COMPASS, "Compass"),
            new FetchurItem(Items.PRISMARINE_CRYSTALS, 20, "Mithril"),
            new FetchurItem(Items.FIREWORK_ROCKET, "Firework Rocket"),
            new FetchurItem(ItemUtils.getTexturedHead("CHEAP_COFFEE").getItemStackTemplate(), "Cheap Coffee"),
            new FetchurItem(Items.OAK_DOOR, "Wooden Door"),
            new FetchurItem(Items.RABBIT_FOOT, 3, "Rabbit's Feet"),
            new FetchurItem(Blocks.TNT.asItem(), "Superboom TNT"),
            new FetchurItem(Blocks.PUMPKIN.asItem(), "Pumpkin"),
            new FetchurItem(Items.FLINT_AND_STEEL, "Flint and Steel"),
            new FetchurItem(Items.EMERALD, 50, "Emerald"),
            //new FetchurItem(new ItemStack(Items.ender_pearl, 16), "Ender Pearl"),
            new FetchurItem(Blocks.RED_WOOL.asItem(), 50, "Red Wool")
    };

    // Used for storage, essential for Fetchur Warner
    @Getter @Setter private FetchurItem currentItemSaved = null;

    /**
     * Get the item Fetchur needs today
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
     * @return {@code true} if the player hasn't yet submitted the item in today (EST).
     */
    public boolean hasFetchedToday() {
        long lastTimeFetched = SkyblockAddons.getInstance().getPersistentValuesManager().getData().getLastTimeFetchur();
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
                main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
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
    public void postPersistentConfigLoad(long lastTimeFetched) {
        long currTime = System.currentTimeMillis();
        // Return true if the days of the month from last submission and current time match
        boolean hasFetchedToday = currTime - lastTimeFetched < MILLISECONDS_IN_A_DAY
                && getFetchurDayOfMonth(lastTimeFetched) == getFetchurDayOfMonth(currTime);
        if (hasFetchedToday) {
            currentItemSaved = getCurrentFetchurItem();
        }
    }

    /**
     * A class representing the item Fetchur wants contains the item instance and the text format of the item
     */
    public record FetchurItem(ItemStackTemplate itemStackTemplate, String itemText) {

        FetchurItem(Item item, String itemText) {
            this(new ItemStackTemplate(item), itemText);
        }

        FetchurItem(Item item, int count, String itemText) {
            this(new ItemStackTemplate(item, count), itemText);
        }

        private static ItemStack itemStack;

        public ItemStack itemStack() {
            if (itemStack == null) {
                itemStack = itemStackTemplate().create();
            }
            return itemStack;
        }

        @Override
        public boolean equals(Object anotherObject) {
            if (anotherObject instanceof FetchurItem(_, String text)) {
                return text.equals(this.itemText());
            }
            return false;
        }

    }
}