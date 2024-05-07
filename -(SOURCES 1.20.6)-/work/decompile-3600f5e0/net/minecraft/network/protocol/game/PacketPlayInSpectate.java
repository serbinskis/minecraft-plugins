package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;

public class PacketPlayInSpectate implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSpectate> STREAM_CODEC = Packet.codec(PacketPlayInSpectate::write, PacketPlayInSpectate::new);
    private final UUID uuid;

    public PacketPlayInSpectate(UUID uuid) {
        this.uuid = uuid;
    }

    private PacketPlayInSpectate(PacketDataSerializer packetdataserializer) {
        this.uuid = packetdataserializer.readUUID();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUUID(this.uuid);
    }

    @Override
    public PacketType<PacketPlayInSpectate> type() {
        return GamePacketTypes.SERVERBOUND_TELEPORT_TO_ENTITY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleTeleportToEntityPacket(this);
    }

    @Nullable
    public Entity getEntity(WorldServer worldserver) {
        return worldserver.getEntity(this.uuid);
    }
}
