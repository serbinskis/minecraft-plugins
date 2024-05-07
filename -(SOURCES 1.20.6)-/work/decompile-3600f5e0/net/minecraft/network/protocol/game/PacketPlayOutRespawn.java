package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutRespawn(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutRespawn> STREAM_CODEC = Packet.codec(PacketPlayOutRespawn::write, PacketPlayOutRespawn::new);
    public static final byte KEEP_ATTRIBUTES = 1;
    public static final byte KEEP_ENTITY_DATA = 2;
    public static final byte KEEP_ALL_DATA = 3;

    private PacketPlayOutRespawn(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this(new CommonPlayerSpawnInfo(registryfriendlybytebuf), registryfriendlybytebuf.readByte());
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.commonPlayerSpawnInfo.write(registryfriendlybytebuf);
        registryfriendlybytebuf.writeByte(this.dataToKeep);
    }

    @Override
    public PacketType<PacketPlayOutRespawn> type() {
        return GamePacketTypes.CLIENTBOUND_RESPAWN;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRespawn(this);
    }

    public boolean shouldKeep(byte b0) {
        return (this.dataToKeep & b0) != 0;
    }
}
