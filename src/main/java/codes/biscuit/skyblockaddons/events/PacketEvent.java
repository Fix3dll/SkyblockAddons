package codes.biscuit.skyblockaddons.events;

import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public abstract class PacketEvent extends Event {
    public abstract Packet<?> getPacket();

    @Getter public static class ReceiveEvent extends PacketEvent {
        private final Packet<?> packet;

        public ReceiveEvent(Packet<?> packet) {
            this.packet = packet;
        }
    }

    @Getter public static class SendEvent extends PacketEvent {
        private final Packet<?> packet;

        public SendEvent(Packet<?> packet) {
            this.packet = packet;
        }
    }

}
