package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public interface CustomPacketPayload {

    void write(PacketDataSerializer packetdataserializer);

    MinecraftKey id();
}
