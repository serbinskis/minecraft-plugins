package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class PacketPlayOutLightUpdate implements Packet<PacketListenerPlayOut> {

    private final int x;
    private final int z;
    private final ClientboundLightUpdatePacketData lightData;

    public PacketPlayOutLightUpdate(ChunkCoordIntPair chunkcoordintpair, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
        this.x = chunkcoordintpair.x;
        this.z = chunkcoordintpair.z;
        this.lightData = new ClientboundLightUpdatePacketData(chunkcoordintpair, levellightengine, bitset, bitset1);
    }

    public PacketPlayOutLightUpdate(PacketDataSerializer packetdataserializer) {
        this.x = packetdataserializer.readVarInt();
        this.z = packetdataserializer.readVarInt();
        this.lightData = new ClientboundLightUpdatePacketData(packetdataserializer, this.x, this.z);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.x);
        packetdataserializer.writeVarInt(this.z);
        this.lightData.write(packetdataserializer);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleLightUpdatePacket(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public ClientboundLightUpdatePacketData getLightData() {
        return this.lightData;
    }
}
