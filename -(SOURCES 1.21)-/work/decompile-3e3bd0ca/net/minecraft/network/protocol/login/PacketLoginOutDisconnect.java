package net.minecraft.network.protocol.login;

import net.minecraft.core.IRegistryCustom;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketLoginOutDisconnect implements Packet<PacketLoginOutListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginOutDisconnect> STREAM_CODEC = Packet.codec(PacketLoginOutDisconnect::write, PacketLoginOutDisconnect::new);
    private final IChatBaseComponent reason;

    public PacketLoginOutDisconnect(IChatBaseComponent ichatbasecomponent) {
        this.reason = ichatbasecomponent;
    }

    private PacketLoginOutDisconnect(PacketDataSerializer packetdataserializer) {
        this.reason = IChatBaseComponent.ChatSerializer.fromJsonLenient(packetdataserializer.readUtf(262144), IRegistryCustom.EMPTY);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(IChatBaseComponent.ChatSerializer.toJson(this.reason, IRegistryCustom.EMPTY));
    }

    @Override
    public PacketType<PacketLoginOutDisconnect> type() {
        return LoginPacketTypes.CLIENTBOUND_LOGIN_DISCONNECT;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleDisconnect(this);
    }

    public IChatBaseComponent getReason() {
        return this.reason;
    }
}
