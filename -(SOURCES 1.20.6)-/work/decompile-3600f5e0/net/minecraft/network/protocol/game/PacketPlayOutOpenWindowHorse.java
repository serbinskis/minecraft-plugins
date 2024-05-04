package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutOpenWindowHorse implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutOpenWindowHorse> STREAM_CODEC = Packet.codec(PacketPlayOutOpenWindowHorse::write, PacketPlayOutOpenWindowHorse::new);
    private final int containerId;
    private final int size;
    private final int entityId;

    public PacketPlayOutOpenWindowHorse(int i, int j, int k) {
        this.containerId = i;
        this.size = j;
        this.entityId = k;
    }

    private PacketPlayOutOpenWindowHorse(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readUnsignedByte();
        this.size = packetdataserializer.readVarInt();
        this.entityId = packetdataserializer.readInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
        packetdataserializer.writeVarInt(this.size);
        packetdataserializer.writeInt(this.entityId);
    }

    @Override
    public PacketType<PacketPlayOutOpenWindowHorse> type() {
        return GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleHorseScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSize() {
        return this.size;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
