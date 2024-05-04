package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.EntityLiving;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundHurtAnimationPacket> STREAM_CODEC = Packet.codec(ClientboundHurtAnimationPacket::write, ClientboundHurtAnimationPacket::new);

    public ClientboundHurtAnimationPacket(EntityLiving entityliving) {
        this(entityliving.getId(), entityliving.getHurtDir());
    }

    private ClientboundHurtAnimationPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readFloat());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeFloat(this.yaw);
    }

    @Override
    public PacketType<ClientboundHurtAnimationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_HURT_ANIMATION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleHurtAnimation(this);
    }
}
