package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInTabComplete implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInTabComplete> STREAM_CODEC = Packet.codec(PacketPlayInTabComplete::write, PacketPlayInTabComplete::new);
    private final int id;
    private final String command;

    public PacketPlayInTabComplete(int i, String s) {
        this.id = i;
        this.command = s;
    }

    private PacketPlayInTabComplete(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
        this.command = packetdataserializer.readUtf(32500);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeUtf(this.command, 32500);
    }

    @Override
    public PacketType<PacketPlayInTabComplete> type() {
        return GamePacketTypes.SERVERBOUND_COMMAND_SUGGESTION;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleCustomCommandSuggestions(this);
    }

    public int getId() {
        return this.id;
    }

    public String getCommand() {
        return this.command;
    }
}
