package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.features.JerryPresent;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;

public class RenderManagerHook {

    private static final int HIDE_RADIUS_SQUARED = 7 * 7;
    private static final String HAUNTED_SKULL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0=";

    public static void shouldRender(Entity entityIn, ReturnValue<Boolean> returnValue) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            Island currentMap = main.getUtils().getMap();

            if (main.getConfigValues().isEnabled(Feature.HIDE_BONES) && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                if (entityIn instanceof EntityItem && entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) {
                    EntityItem entityItem = (EntityItem) entityIn;
                    if (entityItem.getEntityItem().getItem().equals(Items.bone)) {
                        returnValue.cancel();
                    }
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_HAUNTED_SKULLS) && main.getUtils().isInDungeon()) {
                if (entityIn instanceof EntityArmorStand && entityIn.isInvisible()) {
                    EntityArmorStand armorStand = (EntityArmorStand) entityIn;
                    String skullID = ItemUtils.getSkullTexture(armorStand.getEquipmentInSlot(4));
                    if (HAUNTED_SKULL_TEXTURE.equals(skullID)) {
                        returnValue.cancel();
                    }
                }
            }
            if (mc.theWorld != null && Feature.HIDE_PLAYERS_NEAR_NPCS.isEnabled() && !main.getUtils().isGuest()
                    && currentMap != Island.DUNGEON) {
                if (entityIn instanceof EntityOtherPlayerMP && !NPCUtils.isNPC(entityIn) && NPCUtils.isNearNPC(entityIn)) {
                    returnValue.cancel();
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_SPAWN_POINT_PLAYERS)) {
                BlockPos entityPosition = entityIn.getPosition();
                if (entityIn instanceof EntityPlayer && LocationUtils.isOn("Village")
                        && entityPosition.getX() == -2
                        && entityPosition.getY() == 70
                        && entityPosition.getZ() == -69) {
                    returnValue.cancel();
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (LocationUtils.isOn("Village", "Auction House", "Bank")) {
                    if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame) &&
                            !NPCUtils.isNPC(entityIn) && entityIn.getDistanceSqToEntity(mc.thePlayer) > HIDE_RADIUS_SQUARED) {
                        returnValue.cancel();
                    }
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_OTHER_PLAYERS_PRESENTS)) {
                JerryPresent present = JerryPresent.getJerryPresents().get(entityIn.getUniqueID());
                if (present != null && present.shouldHide()) {
                    returnValue.cancel();
                }
            }
        }
    }
}
