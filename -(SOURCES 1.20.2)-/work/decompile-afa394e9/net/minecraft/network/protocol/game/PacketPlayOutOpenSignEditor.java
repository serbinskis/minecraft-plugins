package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public class PacketPlayOutOpenSignEditor implements Packet<PacketListenerPlayOut> {

    private final BlockPosition pos;
    private final boolean isFrontText;

    public PacketPlayOutOpenSignEditor(BlockPosition blockposition, boolean flag) {
        this.pos = blockposition;
        this.isFrontText = flag;
    }

    public PacketPlayOutOpenSignEditor(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.isFrontText = packetdataserializer.readBoolean();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeBoolean(this.isFrontText);
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
