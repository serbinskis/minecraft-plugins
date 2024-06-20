package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class StatusPacketTypes {

    public static final PacketType<PacketStatusOutServerInfo> CLIENTBOUND_STATUS_RESPONSE = createClientbound("status_response");
    public static final PacketType<PacketStatusInStart> SERVERBOUND_STATUS_REQUEST = createServerbound("status_request");

    public StatusPacketTypes() {}

    private static <T extends Packet<PacketStatusOutListener>> PacketType<T> createClientbound(String s) {
        return new PacketType<>(EnumProtocolDirection.CLIENTBOUND, MinecraftKey.withDefaultNamespace(s));
    }

    private static <T extends Packet<PacketStatusInListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, MinecraftKey.withDefaultNamespace(s));
    }
}
