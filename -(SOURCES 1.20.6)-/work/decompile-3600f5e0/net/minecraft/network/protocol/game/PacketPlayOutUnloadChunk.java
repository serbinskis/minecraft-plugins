package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkCoordIntPair;

public record PacketPlayOutUnloadChunk(ChunkCoordIntPair pos) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutUnloadChunk> STREAM_CODEC = Packet.codec(PacketPlayOutUnloadChunk::write, PacketPlayOutUnloadChunk::new);

    private PacketPlayOutUnloadChunk(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readChunkPos());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeChunkPos(this.pos);
    }

    @Override
    public PacketType<PacketPlayOutUnloadChunk> type() {
        return GamePacketTypes.CLIENTBOUND_FORGET_LEVEL_CHUNK;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleForgetLevelChunk(this);
    }
}
