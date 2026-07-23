package com.fix3dll.skyblockaddons.features.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * Base for client-only slots that are drawn through vanilla's per-slot render path but are never registered in an
 * {@link net.minecraft.world.inventory.AbstractContainerMenu}. Going through vanilla lets other mods that hook the
 * per-slot render method decorate these slots too, while staying out of the menu keeps them off every sync path.
 * <p>
 * {@code index} is left at {@code -1}: vanilla reads it as "no slot" when it turns a click into a container input, and
 * anything that does use it to address a menu slot fails loudly instead of quietly sending a packet for a slot the
 * server does not know about.
 */
public abstract class VirtualSlot extends Slot {

    protected VirtualSlot(Container container, int containerSlot, int x, int y) {
        super(container, containerSlot, x, y);
        this.index = -1;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean mayPickup(@NonNull Player player) {
        return false;
    }

}