package net.minecraft.network.protocol.cookie;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class CookiePacketTypes {

    public static final PacketType<ClientboundCookieRequestPacket> CLIENTBOUND_COOKIE_REQUEST = createClientbound("cookie_request");
    public static final PacketType<ServerboundCookieResponsePacket> SERVERBOUND_COOKIE_RESPONSE = createServerbound("cookie_response");

    public CookiePacketTypes() {}

    private static <T extends Packet<ClientCookiePacketListener>> PacketType<T> createClientbound(String s) {
        return new PacketType<>(EnumProtocolDirection.CLIENTBOUND, new MinecraftKey(s));
    }

    private static <T extends Packet<ServerCookiePacketListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, new MinecraftKey(s));
    }
}
