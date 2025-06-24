package com.fix3dll.skyblockaddons.features.outline;

import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent;
import com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent.Type;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Function;

/**
 * Controls the behavior of the {@link Feature#MAKE_DROPPED_ITEMS_GLOW} and {@link FeatureSetting#SHOW_GLOWING_ITEMS_ON_ISLAND} features
 */
public class ItemOutlines {

    /**
     * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
     * Should be used in conjunction with the global-level predicate, {@link #shouldOutline()}.
     * <p>
     * Return {@code null} if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
     */
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        // Only accept items that aren't showcase items
        if (e instanceof ItemEntity itemEntity) {
            // Don't display showcase blocks if player doesn't want them or is outside the building
            if (isShopShowcaseItem(itemEntity)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (Feature.ENTITY_OUTLINES.isDisabled(FeatureSetting.OUTLINE_SHOWCASE_ITEMS)) {
                    return null;
                } else if (player != null && !player.hasLineOfSight(itemEntity)) {
                    return null;
                }
            }
            SkyblockRarity itemRarity = ItemUtils.getRarity(itemEntity.getItem());
            if (itemRarity != null) {
                // Return the rarity color of the item
                return itemRarity.getColorCode().getColor();
            }
            // Return null if the item doesn't have a rarity for some reason...
            return null;
        }
        return null;
    };

    /**
     * Global-level predicate to determine whether any entities should outlined.
     * Should be used in conjunction with the entity-level predicate, {@link #OUTLINE_COLOR}.
     * <p>
     * Don't accept if the player is on a personal island and the
     * @return {@code false} if no entities should be outlined (i.e., accept if the player has item outlines enabled for the current skyblock location)
     */
    private static boolean shouldOutline() {
        Feature feature = Feature.MAKE_DROPPED_ITEMS_GLOW;
        return feature.isEnabled() && (feature.isEnabled(FeatureSetting.SHOW_GLOWING_ITEMS_ON_ISLAND) || !LocationUtils.isOn(Island.PRIVATE_ISLAND));
    }

    /**
     * This method checks if the given EntityItem is an item being showcased in a shop.
     * It works by detecting glass case the item is in.
     * @param entityItem the potential shop showcase item.
     * @return true if the entity is a shop showcase item.
     */
    private static boolean isShopShowcaseItem(ItemEntity entityItem) {
        @SuppressWarnings("resource") List<ArmorStand> showcaseStands = entityItem.level().getEntitiesOfClass(
                ArmorStand.class,
                entityItem.getBoundingBox()/*.inflate(0, 1, 0)*/,
                armorStand -> {
                    if (armorStand.isInvisible()) {
                        Item headItem = armorStand.getItemBySlot(EquipmentSlot.HEAD).getItem();
                        return headItem != Items.AIR && headItem == Blocks.GLASS.asItem();
                    }
                    return false;
                });
        return !showcaseStands.isEmpty();
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     * @param e the outline event
     */
    protected static void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        if (e.getType() == Type.XRAY) {
            // Test whether we should add any entities at all
            if (shouldOutline()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR);
            }
        }
    }

}