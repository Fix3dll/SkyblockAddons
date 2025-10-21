package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.npc.NPCUtils;
import com.fix3dll.skyblockaddons.features.JerryPresent;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class EntityRendererHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    private static final int HIDE_RADIUS_SQUARED = 7 * 7;
    private static final String HAUNTED_SKULL_TEXTURE = ItemUtils.getSkullTexture(ItemUtils.getTexturedHead("HAUNTED_SKULL"));

    public static boolean shouldRender(Entity entityIn) {
        if (main.getUtils().isOnSkyblock()) {
            Island currentMap = main.getUtils().getMap();

            if (Feature.HIDE_BONES.isEnabled() && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                if (entityIn instanceof ItemEntity itemEntity && entityIn.getVehicle() instanceof ArmorStand && entityIn.getVehicle().isInvisible()) {
                    if (itemEntity.getItem().getItem().equals(Items.BONE)) {
                        return false;
                    }
                }
            }
            if (Feature.HIDE_HAUNTED_SKULLS.isEnabled() && main.getUtils().isInDungeon()) {
                if (entityIn instanceof ArmorStand armorStand && armorStand.isInvisible()) {
                    String skullID = ItemUtils.getSkullTexture(armorStand.getItemBySlot(EquipmentSlot.HEAD));
                    if (HAUNTED_SKULL_TEXTURE.equals(skullID)) {
                        return false;
                    }
                }
            }
            if (MC.level != null && Feature.HIDE_PLAYERS_NEAR_NPCS.isEnabled() && !main.getUtils().isGuest()
                    && currentMap != Island.DUNGEON) {
                if (entityIn instanceof RemotePlayer && !NPCUtils.isNPC(entityIn) && NPCUtils.isNearNPC(entityIn)) {
                    return false;
                }
            }
            if (Feature.HIDE_SPAWN_POINT_PLAYERS.isEnabled()) {
                if (entityIn instanceof Player && LocationUtils.isOn("Village")
                        && entityIn.getX() == -2.5D
                        && entityIn.getY() == 70.0D
                        && entityIn.getZ() == -69.5D) {
                    return false;
                }
            }
            if (Feature.HIDE_PLAYERS_IN_LOBBY.isEnabled() && LocationUtils.isOn("Village", "Auction House", "Bank")) {
                if ((entityIn instanceof RemotePlayer || entityIn instanceof ItemFrame)
                        && !NPCUtils.isNPC(entityIn) && entityIn.distanceToSqr(MC.player) > HIDE_RADIUS_SQUARED) {
                    return false;
                }
            }
            if (Feature.HIDE_OTHER_PLAYERS_PRESENTS.isEnabled()) {
                JerryPresent present = JerryPresent.getJerryPresents().get(entityIn.getUUID());
                if (present != null && present.shouldHide()) {
                    return false;
                }
            }

            Component customNameComponent = entityIn.getCustomName();
            if (customNameComponent != null) {
                String formattedText = TextUtils.getFormattedText(customNameComponent);
                if (Feature.MINION_DISABLE_LOCATION_WARNING.isEnabled()) {
                    if (formattedText.startsWith("§cThis location isn't perfect! :(")) {
                        return false;
                    }
                    if (customNameComponent.getString().startsWith("§c/!\\") && MC.level != null) {
                        for (Entity listEntity : MC.level.entitiesForRendering()) {
                            if (listEntity.hasCustomName()
                                    && formattedText.startsWith("§cThis location isn't perfect! :(")
                                    && listEntity.getX() == entityIn.getX()
                                    && listEntity.getZ() == entityIn.getZ()
                                    && listEntity.getY() + 0.375 == entityIn.getY()) {
                                return false;
                            }
                        }
                    }
                }

                if (Feature.HIDE_SVEN_PUP_NAMETAGS.isEnabled() && entityIn instanceof ArmorStand) {
                    return !customNameComponent.getString().contains("Sven Pup");
                }
            }
        }
        return true;
    }

}