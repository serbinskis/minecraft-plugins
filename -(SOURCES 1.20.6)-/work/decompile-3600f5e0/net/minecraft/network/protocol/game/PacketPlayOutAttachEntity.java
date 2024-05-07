package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class PacketPlayOutAttachEntity implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutAttachEntity> STREAM_CODEC = Packet.codec(PacketPlayOutAttachEntity::write, PacketPlayOutAttachEntity::new);
    private final int sourceId;
    private final int destId;

    public PacketPlayOutAttachEntity(Entity entity, @Nullable Entity entity1) {
        this.sourceId = entity.getId();
        this.destId = entity1 != null ? entity1.getId() : 0;
    }

    private PacketPlayOutAttachEntity(PacketDataSerializer packetdataserializer) {
        this.sourceId = packetdataserializer.readInt();
        this.destId = packetdataserializer.readInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.sourceId);
        packetdataserializer.writeInt(this.destId);
    }

    @Override
    public PacketType<PacketPlayOutAttachEntity> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_LINK;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleEntityLinkPacket(this);
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public int getDestId() {
        return this.destId;
    }
}
