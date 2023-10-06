package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record DiscardedPayload(MinecraftKey id) implements CustomPacketPayload {

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}
}
