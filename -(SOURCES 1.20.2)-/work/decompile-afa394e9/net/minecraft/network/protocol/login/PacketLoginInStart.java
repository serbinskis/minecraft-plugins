package net.minecraft.network.protocol.login;

import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record PacketLoginInStart(String name, UUID profileId) implements Packet<PacketLoginInListener> {

    public PacketLoginInStart(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(16), packetdataserializer.readUUID());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.name, 16);
        packetdataserializer.writeUUID(this.profileId);
    }

    public void handle(PacketLoginInListener packetlogininlistener) {
        packetlogininlistener.handleHello(this);
    }
}
