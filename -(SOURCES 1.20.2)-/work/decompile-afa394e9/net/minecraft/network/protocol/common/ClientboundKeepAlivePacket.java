package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class ClientboundKeepAlivePacket implements Packet<ClientCommonPacketListener> {

    private final long id;

    public ClientboundKeepAlivePacket(long i) {
        this.id = i;
    }

    public ClientboundKeepAlivePacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readLong();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.id);
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}
