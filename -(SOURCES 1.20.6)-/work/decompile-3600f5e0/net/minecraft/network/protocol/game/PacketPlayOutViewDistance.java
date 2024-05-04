package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutViewDistance implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutViewDistance> STREAM_CODEC = Packet.codec(PacketPlayOutViewDistance::write, PacketPlayOutViewDistance::new);
    private final int radius;

    public PacketPlayOutViewDistance(int i) {
        this.radius = i;
    }

    private PacketPlayOutViewDistance(PacketDataSerializer packetdataserializer) {
        this.radius = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.radius);
    }

    @Override
    public PacketType<PacketPlayOutViewDistance> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_RADIUS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetChunkCacheRadius(this);
    }

    public int getRadius() {
        return this.radius;
    }
}
