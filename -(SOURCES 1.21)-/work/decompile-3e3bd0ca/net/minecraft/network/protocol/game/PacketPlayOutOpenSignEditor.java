package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutOpenSignEditor implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutOpenSignEditor> STREAM_CODEC = Packet.codec(PacketPlayOutOpenSignEditor::write, PacketPlayOutOpenSignEditor::new);
    private final BlockPosition pos;
    private final boolean isFrontText;

    public PacketPlayOutOpenSignEditor(BlockPosition blockposition, boolean flag) {
        this.pos = blockposition;
        this.isFrontText = flag;
    }

    private PacketPlayOutOpenSignEditor(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.isFrontText = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeBoolean(this.isFrontText);
    }

    @Override
    public PacketType<PacketPlayOutOpenSignEditor> type() {
        return GamePacketTypes.CLIENTBOUND_OPEN_SIGN_EDITOR;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleOpenSignEditor(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public boolean isFrontText() {
        return this.isFrontText;
    }
}
