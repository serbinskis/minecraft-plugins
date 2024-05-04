package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public record DiscardedPayload(MinecraftKey id) implements CustomPacketPayload {

    public static <T extends PacketDataSerializer> StreamCodec<T, DiscardedPayload> codec(MinecraftKey minecraftkey, int i) {
        return CustomPacketPayload.codec((discardedpayload, packetdataserializer) -> {
        }, (packetdataserializer) -> {
            int j = packetdataserializer.readableBytes();

            if (j >= 0 && j <= i) {
                packetdataserializer.skipBytes(j);
                return new DiscardedPayload(minecraftkey);
            } else {
                throw new IllegalArgumentException("Payload may not be larger than " + i + " bytes");
            }
        });
    }

    @Override
    public CustomPacketPayload.b<DiscardedPayload> type() {
        return new CustomPacketPayload.b<>(this.id);
    }
}
