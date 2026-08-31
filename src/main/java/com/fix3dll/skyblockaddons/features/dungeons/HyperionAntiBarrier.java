package com.fix3dll.skyblockaddons.features.dungeons;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.status.EffectInstance;

import java.util.List;

/**
 * Anti-barrier bypass for Hyperion teleport (issue #198).
 *
 * <p>Certain dungeon mobs apply a "barrier" effect that, while visible, blocks
 * Hyperion teleport. When the bypass is enabled, the barrier effect is stripped
 * from the local player every tick so that Hyperion teleport keeps working.
 */
public class HyperionAntiBarrier {

    /**
     * Whether the barrier bypass is enabled. Defaults to {@code true} so that
     * Hyperion teleport works even while the barrier effect is shown.
     */
    private static volatile boolean enabled = true;

    /** Substring used to identify the barrier effect applied by dungeon mobs. */
    private static final String BARRIER_EFFECT_MARKER = "barrier";

    public HyperionAntiBarrier() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    private void onEndClientTick(Minecraft client) {
        if (!enabled) {
            return;
        }
        if (client.player == null || !(client.player instanceof LivingEntity living)) {
            return;
        }

        for (EffectInstance effect : List.copyOf(living.getEffects())) {
            if (isBarrierEffect(effect)) {
                living.removeStatusEffect(effect.getEffect());
            }
        }
    }

    /**
     * @return true if the given effect is the barrier effect applied by dungeon mobs.
     */
    private static boolean isBarrierEffect(EffectInstance effect) {
        String name = effect.getEffect().getName();
        return name != null && name.toLowerCase().contains(BARRIER_EFFECT_MARKER);
    }
}
