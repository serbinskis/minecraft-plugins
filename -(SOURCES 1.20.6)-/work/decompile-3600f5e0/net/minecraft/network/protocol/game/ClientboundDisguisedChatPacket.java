package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundDisguisedChatPacket(IChatBaseComponent message, ChatMessageType.a chatType) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDisguisedChatPacket> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, ClientboundDisguisedChatPacket::message, ChatMessageType.a.STREAM_CODEC, ClientboundDisguisedChatPacket::chatType, ClientboundDisguisedChatPacket::new);

    @Override
    public PacketType<ClientboundDisguisedChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DISGUISED_CHAT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleDisguisedChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
