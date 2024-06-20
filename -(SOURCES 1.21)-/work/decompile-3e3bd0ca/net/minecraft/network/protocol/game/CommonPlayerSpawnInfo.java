package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.World;
import net.minecraft.world.level.dimension.DimensionManager;

public record CommonPlayerSpawnInfo(Holder<DimensionManager> dimensionType, ResourceKey<World> dimension, long seed, EnumGamemode gameType, @Nullable EnumGamemode previousGameType, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown) {

    public CommonPlayerSpawnInfo(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this((Holder) DimensionManager.STREAM_CODEC.decode(registryfriendlybytebuf), registryfriendlybytebuf.readResourceKey(Registries.DIMENSION), registryfriendlybytebuf.readLong(), EnumGamemode.byId(registryfriendlybytebuf.readByte()), EnumGamemode.byNullableId(registryfriendlybytebuf.readByte()), registryfriendlybytebuf.readBoolean(), registryfriendlybytebuf.readBoolean(), registryfriendlybytebuf.readOptional(PacketDataSerializer::readGlobalPos), registryfriendlybytebuf.readVarInt());
    }

    public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        DimensionManager.STREAM_CODEC.encode(registryfriendlybytebuf, this.dimensionType);
        registryfriendlybytebuf.writeResourceKey(this.dimension);
        registryfriendlybytebuf.writeLong(this.seed);
        registryfriendlybytebuf.writeByte(this.gameType.getId());
        registryfriendlybytebuf.writeByte(EnumGamemode.getNullableId(this.previousGameType));
        registryfriendlybytebuf.writeBoolean(this.isDebug);
        registryfriendlybytebuf.writeBoolean(this.isFlat);
        registryfriendlybytebuf.writeOptional(this.lastDeathLocation, PacketDataSerializer::writeGlobalPos);
        registryfriendlybytebuf.writeVarInt(this.portalCooldown);
    }
}
