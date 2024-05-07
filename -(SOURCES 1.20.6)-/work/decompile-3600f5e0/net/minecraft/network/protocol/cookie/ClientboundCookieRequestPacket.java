package net.minecraft.network.protocol.cookie;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public record ClientboundCookieRequestPacket(MinecraftKey key) implements Packet<ClientCookiePacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundCookieRequestPacket> STREAM_CODEC = Packet.codec(ClientboundCookieRequestPacket::write, ClientboundCookieRequestPacket::new);

    private ClientboundCookieRequestPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceLocation());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.key);
    }

    @Override
    public PacketType<ClientboundCookieRequestPacket> type() {
        return CookiePacketTypes.CLIENTBOUND_COOKIE_REQUEST;
    }

    public void handle(ClientCookiePacketListener clientcookiepacketlistener) {
        clientcookiepacketlistener.handleRequestCookie(this);
    }
}
