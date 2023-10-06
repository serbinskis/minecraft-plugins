package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record RaidsDebugPayload(List<BlockPosition> raidCenters) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/raids");

    public RaidsDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readList(PacketDataSerializer::readBlockPos));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.raidCenters, PacketDataSerializer::writeBlockPos);
    }

    @Override
    public MinecraftKey id() {
        return RaidsDebugPayload.ID;
    }
}
