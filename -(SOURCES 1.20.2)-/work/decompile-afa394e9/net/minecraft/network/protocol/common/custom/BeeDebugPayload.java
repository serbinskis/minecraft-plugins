package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;

public record BeeDebugPayload(BeeDebugPayload.a beeInfo) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/bee");

    public BeeDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new BeeDebugPayload.a(packetdataserializer));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.beeInfo.write(packetdataserializer);
    }

    @Override
    public MinecraftKey id() {
        return BeeDebugPayload.ID;
    }

    public static record a(UUID uuid, int id, Vec3D pos, @Nullable PathEntity path, @Nullable BlockPosition hivePos, @Nullable BlockPosition flowerPos, int travelTicks, Set<String> goals, List<BlockPosition> blacklistedHives) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readUUID(), packetdataserializer.readInt(), packetdataserializer.readVec3(), (PathEntity) packetdataserializer.readNullable(PathEntity::createFromStream), (BlockPosition) packetdataserializer.readNullable(PacketDataSerializer::readBlockPos), (BlockPosition) packetdataserializer.readNullable(PacketDataSerializer::readBlockPos), packetdataserializer.readInt(), (Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readBlockPos));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUUID(this.uuid);
            packetdataserializer.writeInt(this.id);
            packetdataserializer.writeVec3(this.pos);
            packetdataserializer.writeNullable(this.path, (packetdataserializer1, pathentity) -> {
                pathentity.writeToStream(packetdataserializer1);
            });
            packetdataserializer.writeNullable(this.hivePos, PacketDataSerializer::writeBlockPos);
            packetdataserializer.writeNullable(this.flowerPos, PacketDataSerializer::writeBlockPos);
            packetdataserializer.writeInt(this.travelTicks);
            packetdataserializer.writeCollection(this.goals, PacketDataSerializer::writeUtf);
            packetdataserializer.writeCollection(this.blacklistedHives, PacketDataSerializer::writeBlockPos);
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
