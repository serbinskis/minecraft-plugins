package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

public record PacketPlayOutLogin(int playerId, boolean hardcore, Set<ResourceKey<World>> levels, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting, CommonPlayerSpawnInfo commonPlayerSpawnInfo) implements Packet<PacketListenerPlayOut> {

    public PacketPlayOutLogin(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readInt(), packetdataserializer.readBoolean(), (Set) packetdataserializer.readCollection(Sets::newHashSetWithExpectedSize, (packetdataserializer1) -> {
            return packetdataserializer1.readResourceKey(Registries.DIMENSION);
        }), packetdataserializer.readVarInt(), packetdataserializer.readVarInt(), packetdataserializer.readVarInt(), packetdataserializer.readBoolean(), packetdataserializer.readBoolean(), packetdataserializer.readBoolean(), new CommonPlayerSpawnInfo(packetdataserializer));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.playerId);
        packetdataserializer.writeBoolean(this.hardcore);
        packetdataserializer.writeCollection(this.levels, PacketDataSerializer::writeResourceKey);
        packetdataserializer.writeVarInt(this.maxPlayers);
        packetdataserializer.writeVarInt(this.chunkRadius);
        packetdataserializer.writeVarInt(this.simulationDistance);
        packetdataserializer.writeBoolean(this.reducedDebugInfo);
        packetdataserializer.writeBoolean(this.showDeathScreen);
        packetdataserializer.writeBoolean(this.doLimitedCrafting);
        this.commonPlayerSpawnInfo.write(packetdataserializer);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleLogin(this);
    }
}
