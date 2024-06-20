package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutWindowData implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutWindowData> STREAM_CODEC = Packet.codec(PacketPlayOutWindowData::write, PacketPlayOutWindowData::new);
    private final int containerId;
    private final int id;
    private final int value;

    public PacketPlayOutWindowData(int i, int j, int k) {
        this.containerId = i;
        this.id = j;
        this.value = k;
    }

    private PacketPlayOutWindowData(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readUnsignedByte();
        this.id = packetdataserializer.readShort();
        this.value = packetdataserializer.readShort();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
        packetdataserializer.writeShort(this.id);
        packetdataserializer.writeShort(this.value);
    }

    @Override
    public PacketType<PacketPlayOutWindowData> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_DATA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleContainerSetData(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getId() {
        return this.id;
    }

    public int getValue() {
        return this.value;
    }
}
