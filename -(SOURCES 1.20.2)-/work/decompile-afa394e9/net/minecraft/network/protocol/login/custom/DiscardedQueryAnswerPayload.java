package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.PacketDataSerializer;

public record DiscardedQueryAnswerPayload() implements CustomQueryAnswerPayload {

    public static final DiscardedQueryAnswerPayload INSTANCE = new DiscardedQueryAnswerPayload();

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}
}
