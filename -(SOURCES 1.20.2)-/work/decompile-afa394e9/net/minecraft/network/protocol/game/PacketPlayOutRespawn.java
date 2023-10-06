package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record PacketPlayOutRespawn(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<PacketListenerPlayOut> {

    public static final byte KEEP_ATTRIBUTES = 1;
    public static final byte KEEP_ENTITY_DATA = 2;
    public static final byte KEEP_ALL_DATA = 3;

    public PacketPlayOutRespawn(PacketDataSerializer packetdataserializer) {
        this(new CommonPlayerSpawnInfo(packetdataserializer), packetdataserializer.readByte());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.commonPlayerSpawnInfo.write(packetdataserializer);
        packetdataserializer.writeByte(this.dataToKeep);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRespawn(this);
    }

    public boolean shouldKeep(byte b0) {
        return (this.dataToKeep & b0) != 0;
    }
}
