package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) implements Packet<PacketListenerPlayIn> {

    public ServerboundChunkBatchReceivedPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readFloat());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.desiredChunksPerTick);
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleChunkBatchReceived(this);
    }
}
