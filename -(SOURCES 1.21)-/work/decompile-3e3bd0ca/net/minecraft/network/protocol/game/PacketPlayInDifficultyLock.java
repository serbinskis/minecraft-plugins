package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInDifficultyLock implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInDifficultyLock> STREAM_CODEC = Packet.codec(PacketPlayInDifficultyLock::write, PacketPlayInDifficultyLock::new);
    private final boolean locked;

    public PacketPlayInDifficultyLock(boolean flag) {
        this.locked = flag;
    }

    private PacketPlayInDifficultyLock(PacketDataSerializer packetdataserializer) {
        this.locked = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBoolean(this.locked);
    }

    @Override
    public PacketType<PacketPlayInDifficultyLock> type() {
        return GamePacketTypes.SERVERBOUND_LOCK_DIFFICULTY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleLockDifficulty(this);
    }

    public boolean isLocked() {
        return this.locked;
    }
}
