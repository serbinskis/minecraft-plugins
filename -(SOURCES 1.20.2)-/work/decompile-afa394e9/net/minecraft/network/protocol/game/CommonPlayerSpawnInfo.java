package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.World;
import net.minecraft.world.level.dimension.DimensionManager;

public record CommonPlayerSpawnInfo(ResourceKey<DimensionManager> dimensionType, ResourceKey<World> dimension, long seed, EnumGamemode gameType, @Nullable EnumGamemode previousGameType, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown) {

    public CommonPlayerSpawnInfo(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceKey(Registries.DIMENSION_TYPE), packetdataserializer.readResourceKey(Registries.DIMENSION), packetdataserializer.readLong(), EnumGamemode.byId(packetdataserializer.readByte()), EnumGamemode.byNullableId(packetdataserializer.readByte()), packetdataserializer.readBoolean(), packetdataserializer.readBoolean(), packetdataserializer.readOptional(PacketDataSerializer::readGlobalPos), packetdataserializer.readVarInt());
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceKey(this.dimensionType);
        packetdataserializer.writeResourceKey(this.dimension);
        packetdataserializer.writeLong(this.seed);
        packetdataserializer.writeByte(this.gameType.getId());
        packetdataserializer.writeByte(EnumGamemode.getNullableId(this.previousGameType));
        packetdataserializer.writeBoolean(this.isDebug);
        packetdataserializer.writeBoolean(this.isFlat);
        packetdataserializer.writeOptional(this.lastDeathLocation, PacketDataSerializer::writeGlobalPos);
        packetdataserializer.writeVarInt(this.portalCooldown);
    }
}
