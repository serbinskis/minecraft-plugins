package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

public record PacketPlayOutLogin(int playerId, boolean hardcore, Set<ResourceKey<World>> levels, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting, CommonPlayerSpawnInfo commonPlayerSpawnInfo, boolean enforcesSecureChat) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutLogin> STREAM_CODEC = Packet.codec(PacketPlayOutLogin::write, PacketPlayOutLogin::new);

    private PacketPlayOutLogin(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this(registryfriendlybytebuf.readInt(), registryfriendlybytebuf.readBoolean(), (Set) registryfriendlybytebuf.readCollection(Sets::newHashSetWithExpectedSize, (packetdataserializer) -> {
            return packetdataserializer.readResourceKey(Registries.DIMENSION);
        }), registryfriendlybytebuf.readVarInt(), registryfriendlybytebuf.readVarInt(), registryfriendlybytebuf.readVarInt(), registryfriendlybytebuf.readBoolean(), registryfriendlybytebuf.readBoolean(), registryfriendlybytebuf.readBoolean(), new CommonPlayerSpawnInfo(registryfriendlybytebuf), registryfriendlybytebuf.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeInt(this.playerId);
        registryfriendlybytebuf.writeBoolean(this.hardcore);
        registryfriendlybytebuf.writeCollection(this.levels, PacketDataSerializer::writeResourceKey);
        registryfriendlybytebuf.writeVarInt(this.maxPlayers);
        registryfriendlybytebuf.writeVarInt(this.chunkRadius);
        registryfriendlybytebuf.writeVarInt(this.simulationDistance);
        registryfriendlybytebuf.writeBoolean(this.reducedDebugInfo);
        registryfriendlybytebuf.writeBoolean(this.showDeathScreen);
        registryfriendlybytebuf.writeBoolean(this.doLimitedCrafting);
        this.commonPlayerSpawnInfo.write(registryfriendlybytebuf);
        registryfriendlybytebuf.writeBoolean(this.enforcesSecureChat);
    }

    @Override
    public PacketType<PacketPlayOutLogin> type() {
        return GamePacketTypes.CLIENTBOUND_LOGIN;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleLogin(this);
    }
}
