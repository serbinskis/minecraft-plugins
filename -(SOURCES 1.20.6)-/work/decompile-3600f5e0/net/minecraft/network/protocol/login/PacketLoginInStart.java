package net.minecraft.network.protocol.login;

import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketLoginInStart(String name, UUID profileId) implements Packet<PacketLoginInListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginInStart> STREAM_CODEC = Packet.codec(PacketLoginInStart::write, PacketLoginInStart::new);

    private PacketLoginInStart(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(16), packetdataserializer.readUUID());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.name, 16);
        packetdataserializer.writeUUID(this.profileId);
    }

    @Override
    public PacketType<PacketLoginInStart> type() {
        return LoginPacketTypes.SERVERBOUND_HELLO;
    }

    public void handle(PacketLoginInListener packetlogininlistener) {
        packetlogininlistener.handleHello(this);
    }
}
