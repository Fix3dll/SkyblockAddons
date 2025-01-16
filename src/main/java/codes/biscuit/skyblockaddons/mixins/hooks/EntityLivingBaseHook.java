package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

public class EntityLivingBaseHook {
    private static final Set<Long> nightVisionEffectsToRemove = new HashSet<>();

    public static boolean onRemovePotionEffect(EntityLivingBase entityLivingBase, int potionID) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // 16 -> Night Vision
        if (potionID == 16 && entityLivingBase == Minecraft.getMinecraft().thePlayer &&
                main.getUtils().isOnSkyblock() && Feature.AVOID_BLINKING_NIGHT_VISION.isEnabled()) {

            long now = System.currentTimeMillis();
            nightVisionEffectsToRemove.add(now);

            main.getScheduler().scheduleTask(scheduledTask -> {
                if (nightVisionEffectsToRemove.remove(now)) {
                    entityLivingBase.removePotionEffect(potionID);
                }
            }, 2);

            return true;
        }

        return false;
    }

    public static void onAddPotionEffect(EntityLivingBase entityLivingBase, PotionEffect potionEffect) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // 16 -> Night Vision, Night Vision Charm duration is 300 ticks...
        if (potionEffect.getPotionID() == 16 && potionEffect.getDuration() == 300 && entityLivingBase == Minecraft.getMinecraft().thePlayer
                && main.getUtils().isOnSkyblock() && Feature.AVOID_BLINKING_NIGHT_VISION.isEnabled()) {
            nightVisionEffectsToRemove.clear();
        }
    }
}
