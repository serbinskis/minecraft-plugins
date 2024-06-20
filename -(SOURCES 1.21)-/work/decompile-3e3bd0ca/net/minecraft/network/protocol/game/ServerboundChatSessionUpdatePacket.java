package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.a chatSession) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, ServerboundChatSessionUpdatePacket> STREAM_CODEC = Packet.codec(ServerboundChatSessionUpdatePacket::write, ServerboundChatSessionUpdatePacket::new);

    private ServerboundChatSessionUpdatePacket(PacketDataSerializer packetdataserializer) {
        this(RemoteChatSession.a.read(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        RemoteChatSession.a.write(packetdataserializer, this.chatSession);
    }

    @Override
    public PacketType<ServerboundChatSessionUpdatePacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT_SESSION_UPDATE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleChatSessionUpdate(this);
    }
}
