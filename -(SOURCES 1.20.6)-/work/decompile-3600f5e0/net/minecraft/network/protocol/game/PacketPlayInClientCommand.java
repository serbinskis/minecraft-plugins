package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInClientCommand implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInClientCommand> STREAM_CODEC = Packet.codec(PacketPlayInClientCommand::write, PacketPlayInClientCommand::new);
    private final PacketPlayInClientCommand.EnumClientCommand action;

    public PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand packetplayinclientcommand_enumclientcommand) {
        this.action = packetplayinclientcommand_enumclientcommand;
    }

    private PacketPlayInClientCommand(PacketDataSerializer packetdataserializer) {
        this.action = (PacketPlayInClientCommand.EnumClientCommand) packetdataserializer.readEnum(PacketPlayInClientCommand.EnumClientCommand.class);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.action);
    }

    @Override
    public PacketType<PacketPlayInClientCommand> type() {
        return GamePacketTypes.SERVERBOUND_CLIENT_COMMAND;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleClientCommand(this);
    }

    public PacketPlayInClientCommand.EnumClientCommand getAction() {
        return this.action;
    }

    public static enum EnumClientCommand {

        PERFORM_RESPAWN, REQUEST_STATS;

        private EnumClientCommand() {}
    }
}
