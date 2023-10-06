package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public interface CustomQueryPayload {

    MinecraftKey id();

    void write(PacketDataSerializer packetdataserializer);
}
