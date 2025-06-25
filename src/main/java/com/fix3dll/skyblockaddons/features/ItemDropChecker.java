package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
/**
 * This class handles the item checking for the Stop Dropping/Selling Rare Items feature.
 * When the player tries to drop or sell an item, {@link #canDropItem(ItemStack, boolean)} is called to check
 * the item against the rarity requirements, the blacklist, and the whitelist.
 * These requirements determine if the item is allowed to be dropped/sold.
 * @see com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData
 */
public class ItemDropChecker {

    private static final long DROP_CONFIRMATION_TIMEOUT = 3000L;

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    // Variables used for checking drop confirmations
    private static ItemStack itemOfLastDropAttempt;
    private static long timeOfLastDropAttempt;
    private static int attemptsRequiredToConfirm;

    /**
     * Checks if this item can be dropped or sold.
     * This method is for items in the inventory, not those in the hotbar.
     * The alert sound will be played if a drop attempt is denied.
     *
     * @param item the item to check
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public static boolean canDropItem(ItemStack item) {
        return canDropItem(item, false);
    }

    /**
     * Checks if the item in this slot can be dropped or sold. The alert sound will be played if a drop attempt is denied.
     *
     * @param slot the inventory slot to check
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public static boolean canDropItem(Slot slot) {
        if (slot != null && slot.hasItem()) {
            return canDropItem(slot.getItem());
        } else {
            return true;
        }
    }

    /**
     * Checks if this item can be dropped or sold. The alert sound will be played if a drop attempt is denied.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public static boolean canDropItem(ItemStack item, boolean itemIsInHotbar) {
        return canDropItem(item, itemIsInHotbar, true);
    }

    /**
     * Checks if this item can be dropped or sold.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @param playAlert plays an alert sound if {@code true} and a drop attempt is denied, otherwise the sound doesn't play
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public static boolean canDropItem(ItemStack item, boolean itemIsInHotbar, boolean playAlert) {
        if (main.getUtils().isOnSkyblock()) {
            String itemID = ItemUtils.getSkyblockItemID(item);
            SkyblockRarity itemRarity = ItemUtils.getRarity(item);

            if (itemID == null) {
                // Allow dropping of Skyblock items without IDs
                return true;
            } else if (itemRarity == null) {
            /*
             If this Skyblock item has an ID but no rarity, allow dropping it.
             This really shouldn't happen but just in case it does, this condition is here.
             */
                return true;
            }

            List<String> blacklist = main.getOnlineData().getDropSettings().getDontDropTheseItems();
            List<String> whitelist = main.getOnlineData().getDropSettings().getAllowDroppingTheseItems();

            if (itemIsInHotbar) {
                if (itemRarity.compareTo(main.getOnlineData().getDropSettings().getMinimumHotbarRarity()) < 0 && !blacklist.contains(itemID)) {
                    return true;
                } else {
                    // Dropping rare non-whitelisted items from the hotbar is not allowed.
                    if (whitelist.contains(itemID)) {
                        return true;
                    } else {
                        if (playAlert) {
                            playAlert();
                        }
                        return false;
                    }
                }
            } else {
                if (itemRarity.compareTo(main.getOnlineData().getDropSettings().getMinimumInventoryRarity()) < 0 && !blacklist.contains(itemID)) {
                    return true;
                } else {
                    /*
                     If the item is above the minimum rarity and not whitelisted, require the player to attempt
                     to drop it three times to confirm they want to drop it.
                    */
                    if (whitelist.contains(itemID)) {
                        return true;
                    } else {
                        return dropConfirmed(item, 3, playAlert);
                    }
                }
            }
        } else if (Feature.DROP_CONFIRMATION.isEnabled(FeatureSetting.DROP_CONFIRMATION_IN_OTHER_GAMES)) {
            return dropConfirmed(item, 2, playAlert);

        } else {
            return true;
        }
    }

    /**
     * Checks if the player has confirmed that they want to drop the given item stack.
     * The player confirms that they want to drop the item when they try to drop it the number of
     * times specified in {@code numberOfActions}.
     *
     * @param item the item stack the player is attempting to drop
     * @param numberOfActions the number of times the player has to drop the item to confirm
     * @param playAlert plays an alert sound if {@code true} and a drop attempt is denied, otherwise the sound doesn't play
     * @return {@code true} if the player has dropped the item enough
     */
    public static boolean dropConfirmed(ItemStack item, int numberOfActions, boolean playAlert) {
        if (item == null) {
            throw new NullPointerException("Item cannot be null!");

        } else if (numberOfActions < 2) {
            throw new IllegalArgumentException("At least two attempts are required.");
        }

        // If there's no drop confirmation active, set up a new one.
        if (itemOfLastDropAttempt == null) {
            itemOfLastDropAttempt = item;
            timeOfLastDropAttempt = Util.getMillis();
            attemptsRequiredToConfirm = numberOfActions - 1;
            onDropConfirmationFail();
            return false;
        }
        else {
            // Reset the current drop confirmation on time out or if the item being dropped changes.
            if (Util.getMillis() - timeOfLastDropAttempt > DROP_CONFIRMATION_TIMEOUT || !ItemStack.matches(item, itemOfLastDropAttempt)) {
                resetDropConfirmation();
                return dropConfirmed(item, numberOfActions, playAlert);

            } else {
                if (attemptsRequiredToConfirm >= 1) {
                    onDropConfirmationFail();
                    return false;

                } else {
                    resetDropConfirmation();
                    return true;
                }
            }
        }
    }

    /**
     * Called whenever a drop confirmation fails due to the player not attempting to drop the item enough times.
     * A message is sent and a sound is played notifying the player how many more times they need to drop the item.
     */
    public static void onDropConfirmationFail() {
        ColorCode colorCode = Feature.DROP_CONFIRMATION.getRestrictedColor();

        if (attemptsRequiredToConfirm >= 2) {
            String multipleAttemptsRequiredMessage = Translations.getMessage("messages.clickMoreTimes", Integer.toString(attemptsRequiredToConfirm));
            Utils.sendMessage(colorCode + multipleAttemptsRequiredMessage);

        } else {
            String oneMoreAttemptRequiredMessage = Translations.getMessage("messages.clickOneMoreTime");
            Utils.sendMessage(colorCode + oneMoreAttemptRequiredMessage);
        }
        playAlert();
        attemptsRequiredToConfirm--;
    }

    /**
     * Plays an alert sound when a drop attempt is denied.
     */
    public static void playAlert() {
        main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
    }

    /**
     * Reset the drop confirmation settings.
     */
    public static void resetDropConfirmation() {
        itemOfLastDropAttempt = null;
        timeOfLastDropAttempt = 0L;
        attemptsRequiredToConfirm = 0;
    }
}