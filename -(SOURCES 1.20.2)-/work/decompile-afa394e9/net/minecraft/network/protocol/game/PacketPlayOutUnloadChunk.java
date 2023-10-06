package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkCoordIntPair;

public record PacketPlayOutUnloadChunk(ChunkCoordIntPair pos) implements Packet<PacketListenerPlayOut> {

    public PacketPlayOutUnloadChunk(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readChunkPos());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeChunkPos(this.pos);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleForgetLevelChunk(this);
    }
}
