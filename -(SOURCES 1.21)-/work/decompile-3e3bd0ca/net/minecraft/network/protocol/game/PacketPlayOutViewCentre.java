package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutViewCentre implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutViewCentre> STREAM_CODEC = Packet.codec(PacketPlayOutViewCentre::write, PacketPlayOutViewCentre::new);
    private final int x;
    private final int z;

    public PacketPlayOutViewCentre(int i, int j) {
        this.x = i;
        this.z = j;
    }

    private PacketPlayOutViewCentre(PacketDataSerializer packetdataserializer) {
        this.x = packetdataserializer.readVarInt();
        this.z = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.x);
        packetdataserializer.writeVarInt(this.z);
    }

    @Override
    public PacketType<PacketPlayOutViewCentre> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetChunkCacheCenter(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}
