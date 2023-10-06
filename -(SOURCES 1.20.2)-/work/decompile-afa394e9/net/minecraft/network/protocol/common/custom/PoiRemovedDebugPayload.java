package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record PoiRemovedDebugPayload(BlockPosition pos) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/poi_removed");

    public PoiRemovedDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
    }

    @Override
    public MinecraftKey id() {
        return PoiRemovedDebugPayload.ID;
    }
}
