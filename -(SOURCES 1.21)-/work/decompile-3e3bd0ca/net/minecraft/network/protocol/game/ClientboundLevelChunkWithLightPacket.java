package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkWithLightPacket> STREAM_CODEC = Packet.codec(ClientboundLevelChunkWithLightPacket::write, ClientboundLevelChunkWithLightPacket::new);
    private final int x;
    private final int z;
    private final ClientboundLevelChunkPacketData chunkData;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLevelChunkWithLightPacket(Chunk chunk, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
        ChunkCoordIntPair chunkcoordintpair = chunk.getPos();

        this.x = chunkcoordintpair.x;
        this.z = chunkcoordintpair.z;
        this.chunkData = new ClientboundLevelChunkPacketData(chunk);
        this.lightData = new ClientboundLightUpdatePacketData(chunkcoordintpair, levellightengine, bitset, bitset1);
    }

    private ClientboundLevelChunkWithLightPacket(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.x = registryfriendlybytebuf.readInt();
        this.z = registryfriendlybytebuf.readInt();
        this.chunkData = new ClientboundLevelChunkPacketData(registryfriendlybytebuf, this.x, this.z);
        this.lightData = new ClientboundLightUpdatePacketData(registryfriendlybytebuf, this.x, this.z);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeInt(this.x);
        registryfriendlybytebuf.writeInt(this.z);
        this.chunkData.write(registryfriendlybytebuf);
        this.lightData.write(registryfriendlybytebuf);
    }

    @Override
    public PacketType<ClientboundLevelChunkWithLightPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleLevelChunkWithLight(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public ClientboundLevelChunkPacketData getChunkData() {
        return this.chunkData;
    }

    public ClientboundLightUpdatePacketData getLightData() {
        return this.lightData;
    }
}
