package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public record ClientboundStoreCookiePacket(MinecraftKey key, byte[] payload) implements Packet<ClientCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundStoreCookiePacket> STREAM_CODEC = Packet.codec(ClientboundStoreCookiePacket::write, ClientboundStoreCookiePacket::new);
    private static final int MAX_PAYLOAD_SIZE = 5120;
    public static final StreamCodec<ByteBuf, byte[]> PAYLOAD_STREAM_CODEC = ByteBufCodecs.byteArray(5120);

    private ClientboundStoreCookiePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceLocation(), (byte[]) ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC.decode(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.key);
        ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC.encode(packetdataserializer, this.payload);
    }

    @Override
    public PacketType<ClientboundStoreCookiePacket> type() {
        return CommonPacketTypes.CLIENTBOUND_STORE_COOKIE;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleStoreCookie(this);
    }
}
