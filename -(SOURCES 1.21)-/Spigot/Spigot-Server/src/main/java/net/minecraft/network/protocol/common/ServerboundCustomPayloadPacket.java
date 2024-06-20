package net.minecraft.network.protocol.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {

    private static final int MAX_PAYLOAD_SIZE = 32767;
    public static final StreamCodec<PacketDataSerializer, ServerboundCustomPayloadPacket> STREAM_CODEC = CustomPacketPayload.codec((minecraftkey) -> {
        return DiscardedPayload.codec(minecraftkey, 32767);
    }, java.util.Collections.emptyList()).map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload); // CraftBukkit - treat all packets the same

    @Override
    public PacketType<ServerboundCustomPayloadPacket> type() {
        return CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD;
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleCustomPayload(this);
    }
}
