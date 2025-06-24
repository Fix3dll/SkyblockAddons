package com.fix3dll.skyblockaddons.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ClientEvents {
    public static final Event<ClientInitialization> AFTER_INITIALIZATION = EventFactory.createArrayBacked(ClientInitialization.class, callbacks -> (minecraftClient) -> {
        ClientInitialization.AFTER_INITIALIZE.setTrue();
        for (ClientInitialization callback : callbacks) {
            callback.afterInitializeClient(minecraftClient);
        }
    });

    public static final Event<BeforeSetScreen> BEFORE_SET_SCREEN = EventFactory.createArrayBacked(BeforeSetScreen.class, callbacks -> (screen) -> {
        boolean canceled = false;

        for (BeforeSetScreen callback : callbacks) {
            canceled = callback.beforeSetScreen(screen);
        }

        return canceled;
    });

    public static final Event<HandleKeybinds> HANDLE_KEYBINDS = EventFactory.createArrayBacked(HandleKeybinds.class, callbacks -> () -> {
        for (HandleKeybinds callback : callbacks) {
            callback.handleKeybinds();
        }
    });

    public static final Event<EntityJoinWorld> ENTITY_JOIN_WORLD = EventFactory.createArrayBacked(EntityJoinWorld.class, callbacks -> (entity, ci) -> {
        for (EntityJoinWorld callback : callbacks) {
            callback.onEntityJoinWorld(entity, ci);
        }
    });

    public static final Event<EntityTick> LIVING_ENTITY_TICK = EventFactory.createArrayBacked(EntityTick.class, callbacks -> (livingEntity) -> {
        for (EntityTick callback : callbacks) {
            callback.onEntityTick(livingEntity);
        }
    });

    public static final Event<PlaySound> PLAY_SOUND = EventFactory.createArrayBacked(PlaySound.class, callbacks -> (sound, ci) -> {
        for (PlaySound callback : callbacks) {
            callback.onPlaySound(sound, ci);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface ClientInitialization {
        MutableBoolean AFTER_INITIALIZE = new MutableBoolean(false);
        void afterInitializeClient(Minecraft client);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface BeforeSetScreen {
        /**
         * @param screen about to open
         * @return true if initialization will be canceled
         */
        boolean beforeSetScreen(Screen screen);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface HandleKeybinds {
        void handleKeybinds();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface EntityJoinWorld {
        void onEntityJoinWorld(Entity entity, CallbackInfo ci);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface EntityTick {
        void onEntityTick(LivingEntity livingEntity);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface PlaySound {
        void onPlaySound(SoundInstance sound, CallbackInfo ci);
    }

}
