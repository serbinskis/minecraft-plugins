package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutEntityDestroy implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutEntityDestroy> STREAM_CODEC = Packet.codec(PacketPlayOutEntityDestroy::write, PacketPlayOutEntityDestroy::new);
    private final IntList entityIds;

    public PacketPlayOutEntityDestroy(IntList intlist) {
        this.entityIds = new IntArrayList(intlist);
    }

    public PacketPlayOutEntityDestroy(int... aint) {
        this.entityIds = new IntArrayList(aint);
    }

    private PacketPlayOutEntityDestroy(PacketDataSerializer packetdataserializer) {
        this.entityIds = packetdataserializer.readIntIdList();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeIntIdList(this.entityIds);
    }

    @Override
    public PacketType<PacketPlayOutEntityDestroy> type() {
        return GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRemoveEntities(this);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }
}
