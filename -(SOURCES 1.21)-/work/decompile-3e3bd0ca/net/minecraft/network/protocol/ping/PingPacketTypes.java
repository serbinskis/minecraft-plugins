package net.minecraft.network.protocol.ping;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class PingPacketTypes {

    public static final PacketType<ClientboundPongResponsePacket> CLIENTBOUND_PONG_RESPONSE = createClientbound("pong_response");
    public static final PacketType<ServerboundPingRequestPacket> SERVERBOUND_PING_REQUEST = createServerbound("ping_request");

    public PingPacketTypes() {}

    private static <T extends Packet<ClientPongPacketListener>> PacketType<T> createClientbound(String s) {
        return new PacketType<>(EnumProtocolDirection.CLIENTBOUND, MinecraftKey.withDefaultNamespace(s));
    }

    private static <T extends Packet<ServerPingPacketListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, MinecraftKey.withDefaultNamespace(s));
    }
}
