package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public record ClientboundDamageEventPacket(int entityId, Holder<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3D> sourcePosition) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDamageEventPacket> STREAM_CODEC = Packet.codec(ClientboundDamageEventPacket::write, ClientboundDamageEventPacket::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<DamageType>> DAMAGE_TYPE_ID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.DAMAGE_TYPE);

    public ClientboundDamageEventPacket(Entity entity, DamageSource damagesource) {
        this(entity.getId(), damagesource.typeHolder(), damagesource.getEntity() != null ? damagesource.getEntity().getId() : -1, damagesource.getDirectEntity() != null ? damagesource.getDirectEntity().getId() : -1, Optional.ofNullable(damagesource.sourcePositionRaw()));
    }

    private ClientboundDamageEventPacket(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this(registryfriendlybytebuf.readVarInt(), (Holder) ClientboundDamageEventPacket.DAMAGE_TYPE_ID_STREAM_CODEC.decode(registryfriendlybytebuf), readOptionalEntityId(registryfriendlybytebuf), readOptionalEntityId(registryfriendlybytebuf), registryfriendlybytebuf.readOptional((packetdataserializer) -> {
            return new Vec3D(packetdataserializer.readDouble(), packetdataserializer.readDouble(), packetdataserializer.readDouble());
        }));
    }

    private static void writeOptionalEntityId(PacketDataSerializer packetdataserializer, int i) {
        packetdataserializer.writeVarInt(i + 1);
    }

    private static int readOptionalEntityId(PacketDataSerializer packetdataserializer) {
        return packetdataserializer.readVarInt() - 1;
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.entityId);
        ClientboundDamageEventPacket.DAMAGE_TYPE_ID_STREAM_CODEC.encode(registryfriendlybytebuf, this.sourceType);
        writeOptionalEntityId(registryfriendlybytebuf, this.sourceCauseId);
        writeOptionalEntityId(registryfriendlybytebuf, this.sourceDirectId);
        registryfriendlybytebuf.writeOptional(this.sourcePosition, (packetdataserializer, vec3d) -> {
            packetdataserializer.writeDouble(vec3d.x());
            packetdataserializer.writeDouble(vec3d.y());
            packetdataserializer.writeDouble(vec3d.z());
        });
    }

    @Override
    public PacketType<ClientboundDamageEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleDamageEvent(this);
    }

    public DamageSource getSource(World world) {
        if (this.sourcePosition.isPresent()) {
            return new DamageSource(this.sourceType, (Vec3D) this.sourcePosition.get());
        } else {
            Entity entity = world.getEntity(this.sourceCauseId);
            Entity entity1 = world.getEntity(this.sourceDirectId);

            return new DamageSource(this.sourceType, entity1, entity);
        }
    }
}
