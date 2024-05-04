package net.minecraft.network.protocol.handshake;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class HandshakePacketTypes {

    public static final PacketType<PacketHandshakingInSetProtocol> CLIENT_INTENTION = createServerbound("intention");

    public HandshakePacketTypes() {}

    private static <T extends Packet<PacketHandshakingInListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, new MinecraftKey(s));
    }
}
