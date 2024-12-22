package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.core.SkyblockRarity;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent.Type;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Function;

/**
 * Controls the behavior of the {@link codes.biscuit.skyblockaddons.core.Feature#MAKE_DROPPED_ITEMS_GLOW} and {@link codes.biscuit.skyblockaddons.core.Feature#SHOW_GLOWING_ITEMS_ON_ISLAND} features
 */
public class FeatureItemOutlines {

    /**
     * Cached value of the client's SkyBlock location
     */
    private static String location;
    /**
     * Cached value of the client's SkyBlock map
     */
    private static Island map;

    /**
     * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
     * Should be used in conjunction with the global-level predicate, {@link #shouldOutline()}.
     * <p>
     * Return {@code null} if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
     */
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        // Only accept items that aren't showcase items
        if (e instanceof EntityItem) {
            EntityItem item = (EntityItem) e;
            // Don't display showcase blocks if player doesn't want them or is outside the building
            if (LocationUtils.getShowcaseLocations().contains(location) || Feature.OUTLINE_SHOWCASE_ITEMS.isDisabled()
                    && isShopShowcaseItem(item)) {
                return null;
            }
            SkyblockRarity itemRarity = ItemUtils.getRarity(item.getEntityItem());
            if (itemRarity != null) {
                // Return the rarity color of the item
                return itemRarity.getColorCode().getColor();
            }
            // Return null if the item doesn't have a rarity for some reason...
            return null;
        }
        return null;
    };

    public FeatureItemOutlines() {
    }

    /**
     * Global-level predicate to determine whether any entities should outlined.
     * Should be used in conjunction with the entity-level predicate, {@link #OUTLINE_COLOR}.
     * <p>
     * Don't accept if the player is on a personal island and the
     * @return {@code false} if no entities should be outlined (i.e., accept if the player has item outlines enabled for the current skyblock location)
     */
    private static boolean shouldOutline() {
        return Feature.MAKE_DROPPED_ITEMS_GLOW.isEnabled()
                && (Feature.SHOW_GLOWING_ITEMS_ON_ISLAND.isEnabled() || map != Island.PRIVATE_ISLAND);
    }

    /**
     * This method checks if the given EntityItem is an item being showcased in a shop.
     * It works by detecting glass case the item is in.
     * @param entityItem the potential shop showcase item.
     * @return true if the entity is a shop showcase item.
     */
    private static boolean isShopShowcaseItem(EntityItem entityItem) {
        for (EntityArmorStand entityArmorStand : entityItem.worldObj.getEntitiesWithinAABB(EntityArmorStand.class, entityItem.getEntityBoundingBox())) {
            if (entityArmorStand.isInvisible() && entityArmorStand.getEquipmentInSlot(4) != null &&
                    entityArmorStand.getEquipmentInSlot(4).getItem() == Item.getItemFromBlock(Blocks.glass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     * @param e the outline event
     */
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        // Cache constants
        SkyblockAddons main = SkyblockAddons.getInstance();
        location = main.getUtils().getLocation();
        map = main.getUtils().getMap();

        if (e.getType() == Type.XRAY) {
            // Test whether we should add any entities at all
            if (shouldOutline()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR);
            }
        }
    }

}
