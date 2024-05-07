package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.player.PlayerAbilities;

public class PacketPlayInAbilities implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInAbilities> STREAM_CODEC = Packet.codec(PacketPlayInAbilities::write, PacketPlayInAbilities::new);
    private static final int FLAG_FLYING = 2;
    private final boolean isFlying;

    public PacketPlayInAbilities(PlayerAbilities playerabilities) {
        this.isFlying = playerabilities.flying;
    }

    private PacketPlayInAbilities(PacketDataSerializer packetdataserializer) {
        byte b0 = packetdataserializer.readByte();

        this.isFlying = (b0 & 2) != 0;
    }

    private void write(PacketDataSerializer packetdataserializer) {
        byte b0 = 0;

        if (this.isFlying) {
            b0 = (byte) (b0 | 2);
        }

        packetdataserializer.writeByte(b0);
    }

    @Override
    public PacketType<PacketPlayInAbilities> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_ABILITIES;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlayerAbilities(this);
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}
