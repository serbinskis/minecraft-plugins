package net.minecraft.network.protocol.cookie;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.resources.MinecraftKey;

public record ServerboundCookieResponsePacket(MinecraftKey key, @Nullable byte[] payload) implements Packet<ServerCookiePacketListener> {

    public static final StreamCodec<PacketDataSerializer, ServerboundCookieResponsePacket> STREAM_CODEC = Packet.codec(ServerboundCookieResponsePacket::write, ServerboundCookieResponsePacket::new);

    private ServerboundCookieResponsePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceLocation(), (byte[]) packetdataserializer.readNullable(ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.key);
        packetdataserializer.writeNullable(this.payload, ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC);
    }

    @Override
    public PacketType<ServerboundCookieResponsePacket> type() {
        return CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE;
    }

    public void handle(ServerCookiePacketListener servercookiepacketlistener) {
        servercookiepacketlistener.handleCookieResponse(this);
    }
}
