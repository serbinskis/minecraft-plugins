package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInJigsawGenerate implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInJigsawGenerate> STREAM_CODEC = Packet.codec(PacketPlayInJigsawGenerate::write, PacketPlayInJigsawGenerate::new);
    private final BlockPosition pos;
    private final int levels;
    private final boolean keepJigsaws;

    public PacketPlayInJigsawGenerate(BlockPosition blockposition, int i, boolean flag) {
        this.pos = blockposition;
        this.levels = i;
        this.keepJigsaws = flag;
    }

    private PacketPlayInJigsawGenerate(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.levels = packetdataserializer.readVarInt();
        this.keepJigsaws = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeVarInt(this.levels);
        packetdataserializer.writeBoolean(this.keepJigsaws);
    }

    @Override
    public PacketType<PacketPlayInJigsawGenerate> type() {
        return GamePacketTypes.SERVERBOUND_JIGSAW_GENERATE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleJigsawGenerate(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public int levels() {
        return this.levels;
    }

    public boolean keepJigsaws() {
        return this.keepJigsaws;
    }
}
