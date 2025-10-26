package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.fishing.FishParticleManager;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleManager;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.player.LocalPlayer;

public class ParticleEngineHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static void onAddParticle(Particle particle) {
        LocalPlayer player = MC.player;

        if (player != null && main.getUtils().isOnSkyblock()) {
            if (main.getUtils().isInDungeon() && Feature.SHOW_HEALING_CIRCLE_WALL.isEnabled()
                    && particle instanceof SuspendedTownParticle && particle.y % 1 == 0.0) {
                HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(particle.x, particle.z));
            } else if (Feature.COLORED_FISHING_PARTICLES.isEnabled() && particle instanceof WakeParticle wakeParticle) {
                FishParticleManager.onFishWakeSpawn(player.fishing, wakeParticle);
            }
        }
    }

}