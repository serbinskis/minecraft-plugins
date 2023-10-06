package net.minecraft.network.protocol.status;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerPingPacketListener;

public class PacketStatusInPing implements Packet<ServerPingPacketListener> {

    private final long time;

    public PacketStatusInPing(long i) {
        this.time = i;
    }

    public PacketStatusInPing(PacketDataSerializer packetdataserializer) {
        this.time = packetdataserializer.readLong();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.time);
    }

    public void handle(ServerPingPacketListener serverpingpacketlistener) {
        serverpingpacketlistener.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}
