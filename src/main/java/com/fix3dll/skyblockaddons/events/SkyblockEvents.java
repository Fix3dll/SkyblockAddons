package com.fix3dll.skyblockaddons.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public final class SkyblockEvents {

    public static final Event<SkyblockJoined> JOINED = EventFactory.createArrayBacked(SkyblockJoined.class, callbacks -> () -> {
        for (SkyblockJoined callback : callbacks) {
            callback.onSkyblockJoined();
        }
    });

    public static final Event<SkyblockLeft> LEFT = EventFactory.createArrayBacked(SkyblockLeft.class, callbacks -> () -> {
        for (SkyblockLeft callback : callbacks) {
            callback.onSkyblockLeft();
        }
    });

    public static final Event<SkyblockPlayerDeath> DUNGEON_PLAYER_DEATH = EventFactory.createArrayBacked(SkyblockPlayerDeath.class, callbacks -> (player, username, cause) -> {
        for (SkyblockPlayerDeath callback : callbacks) {
            callback.onDungeonPlayerDeath(player, username, cause);
        }
    });

    public static final Event<SkyblockPlayerRevive> DUNGEON_PLAYER_REVIVE = EventFactory.createArrayBacked(SkyblockPlayerRevive.class, callbacks -> (revivedPlayer, revivingPlayer) -> {
        for (SkyblockPlayerRevive callback : callbacks) {
            callback.onDungeonPlayerRevive(revivedPlayer, revivingPlayer);
        }
    });

    public static final Event<SkyblockBlockBreak> BLOCK_BREAK = EventFactory.createArrayBacked(SkyblockBlockBreak.class, callbacks -> (pos, breakTime) -> {
        for (SkyblockBlockBreak callback : callbacks) {
            callback.onBlockBreak(pos, breakTime);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockJoined {
        void onSkyblockJoined();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLeft {
        void onSkyblockLeft();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockPlayerDeath {
        void onDungeonPlayerDeath(AbstractClientPlayer player, String username, String cause);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockPlayerRevive {
        void onDungeonPlayerRevive(AbstractClientPlayer revivedPlayer, AbstractClientPlayer revivingPlayer);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockBlockBreak {
        void onBlockBreak(BlockPos pos, long breakTime);
    }
}
