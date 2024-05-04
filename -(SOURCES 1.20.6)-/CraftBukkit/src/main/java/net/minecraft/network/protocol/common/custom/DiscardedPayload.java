package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public record DiscardedPayload(MinecraftKey id, io.netty.buffer.ByteBuf data) implements CustomPacketPayload { // CraftBukkit - store data

    public static <T extends PacketDataSerializer> StreamCodec<T, DiscardedPayload> codec(MinecraftKey minecraftkey, int i) {
        return CustomPacketPayload.codec((discardedpayload, packetdataserializer) -> {
            packetdataserializer.writeBytes(discardedpayload.data); // CraftBukkit - serialize
        }, (packetdataserializer) -> {
            int j = packetdataserializer.readableBytes();

            if (j >= 0 && j <= i) {
                // CraftBukkit start
                return new DiscardedPayload(minecraftkey, packetdataserializer.readBytes(j));
                // CraftBukkit end
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
