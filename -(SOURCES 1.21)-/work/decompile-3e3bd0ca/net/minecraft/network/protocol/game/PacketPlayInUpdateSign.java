package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInUpdateSign implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInUpdateSign> STREAM_CODEC = Packet.codec(PacketPlayInUpdateSign::write, PacketPlayInUpdateSign::new);
    private static final int MAX_STRING_LENGTH = 384;
    private final BlockPosition pos;
    private final String[] lines;
    private final boolean isFrontText;

    public PacketPlayInUpdateSign(BlockPosition blockposition, boolean flag, String s, String s1, String s2, String s3) {
        this.pos = blockposition;
        this.isFrontText = flag;
        this.lines = new String[]{s, s1, s2, s3};
    }

    private PacketPlayInUpdateSign(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.isFrontText = packetdataserializer.readBoolean();
        this.lines = new String[4];

        for (int i = 0; i < 4; ++i) {
            this.lines[i] = packetdataserializer.readUtf(384);
        }

    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeBoolean(this.isFrontText);

        for (int i = 0; i < 4; ++i) {
            packetdataserializer.writeUtf(this.lines[i]);
        }

    }

    @Override
    public PacketType<PacketPlayInUpdateSign> type() {
        return GamePacketTypes.SERVERBOUND_SIGN_UPDATE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSignUpdate(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public boolean isFrontText() {
        return this.isFrontText;
    }

    public String[] getLines() {
        return this.lines;
    }
}
