package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

@Getter
public class SlayerArmorProgress {

    /** The itemstack that this progress is representing. */
    private final ItemStack itemStack;

    /** The current slayer progress % of the item. */
    private MutableComponent percent;

    private int percentWidth;

    /** The current slayer defence reward of the item. */
    private MutableComponent defence;

    private int defenceWidth;

    public SlayerArmorProgress(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack.getItem()); // Cloned because we change the helmet color later.
        this.percent = Component.literal("55");
        this.defence = Component.literal("40\uE008").withColor(ColorCode.GREEN.getColor());
        SkyblockAddons.getInstance().getScheduler().scheduleTask(scheduledTask -> {
            if (!SkyblockAddons.getInstance().isFullyInitialized()) return;
            this.percentWidth = Minecraft.getInstance().font.width(percent);
            this.defenceWidth = Minecraft.getInstance().font.width(defence);
            scheduledTask.cancel();
        }, 1, 1);

        setHelmetColor();
    }

    public SlayerArmorProgress(ItemStack itemStack, String percent, String defence) {
        this.itemStack = itemStack;
        this.percent = Component.literal(percent);
        this.defence = Component.literal(defence).withColor(ColorCode.GREEN.getColor());
        this.percentWidth = Minecraft.getInstance().font.width(percent);
        this.defenceWidth = Minecraft.getInstance().font.width(defence);
    }

    public void setPercent(String percent) {
        this.percent = Component.literal(percent);
        this.percentWidth = Minecraft.getInstance().font.width(percent);
    }

    public void setDefence(String defence) {
        this.defence = Component.literal(defence).withColor(ColorCode.GREEN.getColor());
        this.defenceWidth = Minecraft.getInstance().font.width(defence);
    }

    private void setHelmetColor() {
        if (itemStack.getItem().equals(Items.LEATHER_HELMET)) {
            itemStack.set(DataComponents.DYED_COLOR,  new DyedItemColor(ColorCode.BLACK.getColor()));
        }
    }

}