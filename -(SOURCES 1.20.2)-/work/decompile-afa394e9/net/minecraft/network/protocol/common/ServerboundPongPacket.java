package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerCommonPacketListener> {

    private final int id;

    public ServerboundPongPacket(int i) {
        this.id = i;
    }

    public ServerboundPongPacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readInt();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.id);
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handlePong(this);
    }

    public int getId() {
        return this.id;
    }
}
