package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class LoginPacketTypes {

    public static final PacketType<PacketLoginOutCustomPayload> CLIENTBOUND_CUSTOM_QUERY = createClientbound("custom_query");
    public static final PacketType<PacketLoginOutSuccess> CLIENTBOUND_GAME_PROFILE = createClientbound("game_profile");
    public static final PacketType<PacketLoginOutEncryptionBegin> CLIENTBOUND_HELLO = createClientbound("hello");
    public static final PacketType<PacketLoginOutSetCompression> CLIENTBOUND_LOGIN_COMPRESSION = createClientbound("login_compression");
    public static final PacketType<PacketLoginOutDisconnect> CLIENTBOUND_LOGIN_DISCONNECT = createClientbound("login_disconnect");
    public static final PacketType<ServerboundCustomQueryAnswerPacket> SERVERBOUND_CUSTOM_QUERY_ANSWER = createServerbound("custom_query_answer");
    public static final PacketType<PacketLoginInStart> SERVERBOUND_HELLO = createServerbound("hello");
    public static final PacketType<PacketLoginInEncryptionBegin> SERVERBOUND_KEY = createServerbound("key");
    public static final PacketType<ServerboundLoginAcknowledgedPacket> SERVERBOUND_LOGIN_ACKNOWLEDGED = createServerbound("login_acknowledged");

    public LoginPacketTypes() {}

    private static <T extends Packet<PacketLoginOutListener>> PacketType<T> createClientbound(String s) {
        return new PacketType<>(EnumProtocolDirection.CLIENTBOUND, new MinecraftKey(s));
    }

    private static <T extends Packet<PacketLoginInListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, new MinecraftKey(s));
    }
}
