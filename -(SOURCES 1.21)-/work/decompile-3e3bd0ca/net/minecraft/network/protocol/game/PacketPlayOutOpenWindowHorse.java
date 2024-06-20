package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutOpenWindowHorse implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutOpenWindowHorse> STREAM_CODEC = Packet.codec(PacketPlayOutOpenWindowHorse::write, PacketPlayOutOpenWindowHorse::new);
    private final int containerId;
    private final int inventoryColumns;
    private final int entityId;

    public PacketPlayOutOpenWindowHorse(int i, int j, int k) {
        this.containerId = i;
        this.inventoryColumns = j;
        this.entityId = k;
    }

    private PacketPlayOutOpenWindowHorse(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readUnsignedByte();
        this.inventoryColumns = packetdataserializer.readVarInt();
        this.entityId = packetdataserializer.readInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
        packetdataserializer.writeVarInt(this.inventoryColumns);
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

    public int getInventoryColumns() {
        return this.inventoryColumns;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
