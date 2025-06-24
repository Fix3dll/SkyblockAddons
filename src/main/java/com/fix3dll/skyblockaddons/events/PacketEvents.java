package com.fix3dll.skyblockaddons.events;

import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.Packet;

public class PacketEvents {

    public static final Event<PacketReadEvent> READ = EventFactory.createArrayBacked(PacketReadEvent.class, callbacks -> (context, packet) -> {
        for (PacketReadEvent callback : callbacks) {
            callback.onPacketRead(context, packet);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface PacketReadEvent {
        void onPacketRead(ChannelHandlerContext context, Packet<?> packet);
    }

}