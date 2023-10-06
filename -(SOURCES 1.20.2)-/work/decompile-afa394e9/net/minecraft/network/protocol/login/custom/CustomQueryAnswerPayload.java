package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.PacketDataSerializer;

public interface CustomQueryAnswerPayload {

    void write(PacketDataSerializer packetdataserializer);
}
