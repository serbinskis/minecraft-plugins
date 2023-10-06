package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {

    private final long id;

    public ServerboundKeepAlivePacket(long i) {
        this.id = i;
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleKeepAlive(this);
    }

    public ServerboundKeepAlivePacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readLong();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.id);
    }

    public long getId() {
        return this.id;
    }
}
