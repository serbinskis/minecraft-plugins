package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumDifficulty;

public class PacketPlayOutServerDifficulty implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutServerDifficulty> STREAM_CODEC = Packet.codec(PacketPlayOutServerDifficulty::write, PacketPlayOutServerDifficulty::new);
    private final EnumDifficulty difficulty;
    private final boolean locked;

    public PacketPlayOutServerDifficulty(EnumDifficulty enumdifficulty, boolean flag) {
        this.difficulty = enumdifficulty;
        this.locked = flag;
    }

    private PacketPlayOutServerDifficulty(PacketDataSerializer packetdataserializer) {
        this.difficulty = EnumDifficulty.byId(packetdataserializer.readUnsignedByte());
        this.locked = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.difficulty.getId());
        packetdataserializer.writeBoolean(this.locked);
    }

    @Override
    public PacketType<PacketPlayOutServerDifficulty> type() {
        return GamePacketTypes.CLIENTBOUND_CHANGE_DIFFICULTY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChangeDifficulty(this);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }
}
