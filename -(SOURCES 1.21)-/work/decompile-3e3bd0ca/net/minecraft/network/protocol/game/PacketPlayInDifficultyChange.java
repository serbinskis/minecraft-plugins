package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumDifficulty;

public class PacketPlayInDifficultyChange implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInDifficultyChange> STREAM_CODEC = Packet.codec(PacketPlayInDifficultyChange::write, PacketPlayInDifficultyChange::new);
    private final EnumDifficulty difficulty;

    public PacketPlayInDifficultyChange(EnumDifficulty enumdifficulty) {
        this.difficulty = enumdifficulty;
    }

    private PacketPlayInDifficultyChange(PacketDataSerializer packetdataserializer) {
        this.difficulty = EnumDifficulty.byId(packetdataserializer.readUnsignedByte());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.difficulty.getId());
    }

    @Override
    public PacketType<PacketPlayInDifficultyChange> type() {
        return GamePacketTypes.SERVERBOUND_CHANGE_DIFFICULTY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleChangeDifficulty(this);
    }

    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }
}
