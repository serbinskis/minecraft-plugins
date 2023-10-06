package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientPongPacketListener;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class PacketStatusOutPong implements Packet<ClientPongPacketListener> {

    private final long time;

    public PacketStatusOutPong(long i) {
        this.time = i;
    }

    public PacketStatusOutPong(PacketDataSerializer packetdataserializer) {
        this.time = packetdataserializer.readLong();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.time);
    }

    public void handle(ClientPongPacketListener clientpongpacketlistener) {
        clientpongpacketlistener.handlePongResponse(this);
    }

    public long getTime() {
        return this.time;
    }
}
