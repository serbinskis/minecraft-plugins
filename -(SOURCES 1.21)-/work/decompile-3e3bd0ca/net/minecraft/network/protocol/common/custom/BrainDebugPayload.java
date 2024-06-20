package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;

public record BrainDebugPayload(BrainDebugPayload.a brainDump) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, BrainDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BrainDebugPayload::write, BrainDebugPayload::new);
    public static final CustomPacketPayload.b<BrainDebugPayload> TYPE = CustomPacketPayload.createType("debug/brain");

    private BrainDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new BrainDebugPayload.a(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        this.brainDump.write(packetdataserializer);
    }

    @Override
    public CustomPacketPayload.b<BrainDebugPayload> type() {
        return BrainDebugPayload.TYPE;
    }

    public static record a(UUID uuid, int id, String name, String profession, int xp, float health, float maxHealth, Vec3D pos, String inventory, @Nullable PathEntity path, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPosition> pois, Set<BlockPosition> potentialPois) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readUUID(), packetdataserializer.readInt(), packetdataserializer.readUtf(), packetdataserializer.readUtf(), packetdataserializer.readInt(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readVec3(), packetdataserializer.readUtf(), (PathEntity) packetdataserializer.readNullable(PathEntity::createFromStream), packetdataserializer.readBoolean(), packetdataserializer.readInt(), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), (Set) packetdataserializer.readCollection(HashSet::new, BlockPosition.STREAM_CODEC), (Set) packetdataserializer.readCollection(HashSet::new, BlockPosition.STREAM_CODEC));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUUID(this.uuid);
            packetdataserializer.writeInt(this.id);
            packetdataserializer.writeUtf(this.name);
            packetdataserializer.writeUtf(this.profession);
            packetdataserializer.writeInt(this.xp);
            packetdataserializer.writeFloat(this.health);
            packetdataserializer.writeFloat(this.maxHealth);
            packetdataserializer.writeVec3(this.pos);
            packetdataserializer.writeUtf(this.inventory);
            packetdataserializer.writeNullable(this.path, (packetdataserializer1, pathentity) -> {
                pathentity.writeToStream(packetdataserializer1);
            });
            packetdataserializer.writeBoolean(this.wantsGolem);
            packetdataserializer.writeInt(this.angerLevel);
            packetdataserializer.writeCollection(this.activities, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.behaviors, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.memories, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.gossips, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.pois, BlockPosition.STREAM_CODEC);
            packetdataserializer.writeCollection(this.potentialPois, BlockPosition.STREAM_CODEC);
        }

        public boolean hasPoi(BlockPosition blockposition) {
            return this.pois.contains(blockposition);
        }

        public boolean hasPotentialPoi(BlockPosition blockposition) {
            return this.potentialPois.contains(blockposition);
        }
    }
}
