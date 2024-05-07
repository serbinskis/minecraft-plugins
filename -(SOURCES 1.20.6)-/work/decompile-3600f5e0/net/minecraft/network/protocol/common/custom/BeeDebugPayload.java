package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;

public record BeeDebugPayload(BeeDebugPayload.a beeInfo) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, BeeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BeeDebugPayload::write, BeeDebugPayload::new);
    public static final CustomPacketPayload.b<BeeDebugPayload> TYPE = CustomPacketPayload.createType("debug/bee");

    private BeeDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new BeeDebugPayload.a(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        this.beeInfo.write(packetdataserializer);
    }

    @Override
    public CustomPacketPayload.b<BeeDebugPayload> type() {
        return BeeDebugPayload.TYPE;
    }

    public static record a(UUID uuid, int id, Vec3D pos, @Nullable PathEntity path, @Nullable BlockPosition hivePos, @Nullable BlockPosition flowerPos, int travelTicks, Set<String> goals, List<BlockPosition> blacklistedHives) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readUUID(), packetdataserializer.readInt(), packetdataserializer.readVec3(), (PathEntity) packetdataserializer.readNullable(PathEntity::createFromStream), (BlockPosition) packetdataserializer.readNullable(BlockPosition.STREAM_CODEC), (BlockPosition) packetdataserializer.readNullable(BlockPosition.STREAM_CODEC), packetdataserializer.readInt(), (Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readUtf), packetdataserializer.readList(BlockPosition.STREAM_CODEC));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUUID(this.uuid);
            packetdataserializer.writeInt(this.id);
            packetdataserializer.writeVec3(this.pos);
            packetdataserializer.writeNullable(this.path, (packetdataserializer1, pathentity) -> {
                pathentity.writeToStream(packetdataserializer1);
            });
            packetdataserializer.writeNullable(this.hivePos, BlockPosition.STREAM_CODEC);
            packetdataserializer.writeNullable(this.flowerPos, BlockPosition.STREAM_CODEC);
            packetdataserializer.writeInt(this.travelTicks);
            packetdataserializer.writeCollection(this.goals, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.blacklistedHives, BlockPosition.STREAM_CODEC);
        }

        public boolean hasHive(BlockPosition blockposition) {
            return Objects.equals(blockposition, this.hivePos);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        public String toString() {
            return this.generateName();
        }
    }
}
