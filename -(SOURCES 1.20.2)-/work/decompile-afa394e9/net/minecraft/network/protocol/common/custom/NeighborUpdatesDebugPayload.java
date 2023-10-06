package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record NeighborUpdatesDebugPayload(long time, BlockPosition pos) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/neighbors_update");

    public NeighborUpdatesDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarLong(), packetdataserializer.readBlockPos());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarLong(this.time);
        packetdataserializer.writeBlockPos(this.pos);
    }

    @Override
    public MinecraftKey id() {
        return NeighborUpdatesDebugPayload.ID;
    }
}
