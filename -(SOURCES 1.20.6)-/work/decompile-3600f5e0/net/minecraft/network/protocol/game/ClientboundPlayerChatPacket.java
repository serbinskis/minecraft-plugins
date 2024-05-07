package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPlayerChatPacket(UUID sender, int index, @Nullable MessageSignature signature, SignedMessageBody.a body, @Nullable IChatBaseComponent unsignedContent, FilterMask filterMask, ChatMessageType.a chatType) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerChatPacket> STREAM_CODEC = Packet.codec(ClientboundPlayerChatPacket::write, ClientboundPlayerChatPacket::new);

    private ClientboundPlayerChatPacket(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this(registryfriendlybytebuf.readUUID(), registryfriendlybytebuf.readVarInt(), (MessageSignature) registryfriendlybytebuf.readNullable(MessageSignature::read), new SignedMessageBody.a(registryfriendlybytebuf), (IChatBaseComponent) PacketDataSerializer.readNullable(registryfriendlybytebuf, ComponentSerialization.TRUSTED_STREAM_CODEC), FilterMask.read(registryfriendlybytebuf), (ChatMessageType.a) ChatMessageType.a.STREAM_CODEC.decode(registryfriendlybytebuf));
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeUUID(this.sender);
        registryfriendlybytebuf.writeVarInt(this.index);
        registryfriendlybytebuf.writeNullable(this.signature, MessageSignature::write);
        this.body.write(registryfriendlybytebuf);
        PacketDataSerializer.writeNullable(registryfriendlybytebuf, this.unsignedContent, ComponentSerialization.TRUSTED_STREAM_CODEC);
        FilterMask.write(registryfriendlybytebuf, this.filterMask);
        ChatMessageType.a.STREAM_CODEC.encode(registryfriendlybytebuf, this.chatType);
    }

    @Override
    public PacketType<ClientboundPlayerChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_CHAT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
