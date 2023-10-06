package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record DiscardedQueryPayload(MinecraftKey id) implements CustomQueryPayload {

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}
}
