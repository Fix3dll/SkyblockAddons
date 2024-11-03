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
    private static final String HAUNTED_SKULL_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ==";

    public static void shouldRender(Entity entityIn, ReturnValue<Boolean> returnValue) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            Island currentMap = main.getUtils().getMap();

            if (Feature.HIDE_BONES.isEnabled() && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                if (entityIn instanceof EntityItem && entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) {
                    EntityItem entityItem = (EntityItem) entityIn;
                    if (entityItem.getEntityItem().getItem().equals(Items.bone)) {
                        returnValue.cancel();
                    }
                }
            }
            if (Feature.HIDE_HAUNTED_SKULLS.isEnabled() && main.getUtils().isInDungeon()) {
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
            if (Feature.HIDE_SPAWN_POINT_PLAYERS.isEnabled()) {
                BlockPos entityPosition = entityIn.getPosition();
                if (entityIn instanceof EntityPlayer && LocationUtils.isOn("Village")
                        && entityPosition.getX() == -2
                        && entityPosition.getY() == 70
                        && entityPosition.getZ() == -69) {
                    returnValue.cancel();
                }
            }
            if (Feature.HIDE_PLAYERS_IN_LOBBY.isEnabled() && LocationUtils.isOn("Village", "Auction House", "Bank")) {
                if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame)
                        && !NPCUtils.isNPC(entityIn) && entityIn.getDistanceSqToEntity(mc.thePlayer) > HIDE_RADIUS_SQUARED) {
                    returnValue.cancel();
                }
            }
            if (Feature.HIDE_OTHER_PLAYERS_PRESENTS.isEnabled()) {
                JerryPresent present = JerryPresent.getJerryPresents().get(entityIn.getUniqueID());
                if (present != null && present.shouldHide()) {
                    returnValue.cancel();
                }
            }
        }
    }
}
