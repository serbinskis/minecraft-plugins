package net.minecraft.network.protocol.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.MinecraftKey;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {

    private static final int MAX_PAYLOAD_SIZE = 32767;
    private static final Map<MinecraftKey, PacketDataSerializer.a<? extends CustomPacketPayload>> KNOWN_TYPES = ImmutableMap.builder().put(BrandPayload.ID, BrandPayload::new).build();

    public ServerboundCustomPayloadPacket(PacketDataSerializer packetdataserializer) {
        this(readPayload(packetdataserializer.readResourceLocation(), packetdataserializer));
    }

    private static CustomPacketPayload readPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        PacketDataSerializer.a<? extends CustomPacketPayload> packetdataserializer_a = (PacketDataSerializer.a) ServerboundCustomPayloadPacket.KNOWN_TYPES.get(minecraftkey);

        return (CustomPacketPayload) (packetdataserializer_a != null ? (CustomPacketPayload) packetdataserializer_a.apply(packetdataserializer) : readUnknownPayload(minecraftkey, packetdataserializer));
    }

    private static DiscardedPayload readUnknownPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        int i = packetdataserializer.readableBytes();

        if (i >= 0 && i <= 32767) {
            packetdataserializer.skipBytes(i);
            return new DiscardedPayload(minecraftkey);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.payload.id());
        this.payload.write(packetdataserializer);
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleCustomPayload(this);
    }
}
