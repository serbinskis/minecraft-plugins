package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInTeleportAccept implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInTeleportAccept> STREAM_CODEC = Packet.codec(PacketPlayInTeleportAccept::write, PacketPlayInTeleportAccept::new);
    private final int id;

    public PacketPlayInTeleportAccept(int i) {
        this.id = i;
    }

    private PacketPlayInTeleportAccept(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
    }

    @Override
    public PacketType<PacketPlayInTeleportAccept> type() {
        return GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleAcceptTeleportPacket(this);
    }

    public int getId() {
        return this.id;
    }
}
