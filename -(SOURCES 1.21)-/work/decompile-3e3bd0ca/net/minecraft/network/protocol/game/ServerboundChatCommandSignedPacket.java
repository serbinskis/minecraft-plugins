package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatCommandSignedPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.b lastSeenMessages) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, ServerboundChatCommandSignedPacket> STREAM_CODEC = Packet.codec(ServerboundChatCommandSignedPacket::write, ServerboundChatCommandSignedPacket::new);

    private ServerboundChatCommandSignedPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), packetdataserializer.readInstant(), packetdataserializer.readLong(), new ArgumentSignatures(packetdataserializer), new LastSeenMessages.b(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.command);
        packetdataserializer.writeInstant(this.timeStamp);
        packetdataserializer.writeLong(this.salt);
        this.argumentSignatures.write(packetdataserializer);
        this.lastSeenMessages.write(packetdataserializer);
    }

    @Override
    public PacketType<ServerboundChatCommandSignedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSignedChatCommand(this);
    }
}
