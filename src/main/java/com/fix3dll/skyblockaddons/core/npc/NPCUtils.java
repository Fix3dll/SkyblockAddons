package com.fix3dll.skyblockaddons.core.npc;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * This is a set of utility methods relating to Skyblock NPCs
 */
public class NPCUtils {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final int HIDE_RADIUS_SQUARED = (int) Math.round(2.5 * 2.5);

    @Getter private static final Int2ObjectOpenHashMap<Vec3> npcLocations = new Int2ObjectOpenHashMap<>();

    /**
     * Checks if the NPC is a merchant with both buying and selling capabilities
     * @param inventory The inventory to check
     * @return {@code true} if the NPC is a merchant with buying and selling capabilities, {@code false} otherwise
     */
    public static boolean isSellMerchant(NonNullList<Slot> inventory) {
        //TODO Fix for Hypixel localization
        if (MC.player == null) return false;

        final int playerInventorySize =  MC.player.getInventory().getContainerSize() - Inventory.EQUIPMENT_SLOT_MAPPING.size();
        final int sellSlot = inventory.size() - playerInventorySize  - 4 - 1;
        if (sellSlot >= inventory.size() || sellSlot < 0) return false;
        ItemStack itemStack = inventory.get(sellSlot).getItem();

        if (itemStack != ItemStack.EMPTY && itemStack.getCustomName() != null) {
            if (itemStack.getItem() == Blocks.HOPPER.asItem() && itemStack.getCustomName().getString().equals("Sell Item")) {
                return true;
            }

            List<String> tooltip = ItemUtils.getItemLore(itemStack);
            for (String line : tooltip) {
                if (TextUtils.stripColor(line).equals("Click to buyback!")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a given entity is near any NPC.
     * @param entityToCheck The entity to check
     * @return {@code true} if the entity is near an NPC, {@code false} otherwise
     */
    public static boolean isNearNPC(Entity entityToCheck) {
        for (Vec3 npcLocation : npcLocations.values()) {
            if (entityToCheck.distanceToSqr(npcLocation) <= HIDE_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given entity is an NPC
     * @param entity the entity to check
     * @return {@code true} if the entity is an NPC, {@code false} otherwise
     */
    public static boolean isNPC(Entity entity) {
        if (!(entity instanceof RemotePlayer remotePlayer)) {
            return false;
        }

        return entity.getUUID().version() == 2 && remotePlayer.getHealth() == 20.0F && !remotePlayer.isSleeping();
    }

}