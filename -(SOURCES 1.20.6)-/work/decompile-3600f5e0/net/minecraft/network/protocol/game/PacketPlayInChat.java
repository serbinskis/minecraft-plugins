package net.minecraft.network.protocol.game;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayInChat(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.b lastSeenMessages) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInChat> STREAM_CODEC = Packet.codec(PacketPlayInChat::write, PacketPlayInChat::new);

    private PacketPlayInChat(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(256), packetdataserializer.readInstant(), packetdataserializer.readLong(), (MessageSignature) packetdataserializer.readNullable(MessageSignature::read), new LastSeenMessages.b(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.message, 256);
        packetdataserializer.writeInstant(this.timeStamp);
        packetdataserializer.writeLong(this.salt);
        packetdataserializer.writeNullable(this.signature, MessageSignature::write);
        this.lastSeenMessages.write(packetdataserializer);
    }

    @Override
    public PacketType<PacketPlayInChat> type() {
        return GamePacketTypes.SERVERBOUND_CHAT;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleChat(this);
    }
}
