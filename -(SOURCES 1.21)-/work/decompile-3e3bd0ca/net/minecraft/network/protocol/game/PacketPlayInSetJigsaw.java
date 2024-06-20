package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.block.entity.TileEntityJigsaw;

public class PacketPlayInSetJigsaw implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInSetJigsaw> STREAM_CODEC = Packet.codec(PacketPlayInSetJigsaw::write, PacketPlayInSetJigsaw::new);
    private final BlockPosition pos;
    private final MinecraftKey name;
    private final MinecraftKey target;
    private final MinecraftKey pool;
    private final String finalState;
    private final TileEntityJigsaw.JointType joint;
    private final int selectionPriority;
    private final int placementPriority;

    public PacketPlayInSetJigsaw(BlockPosition blockposition, MinecraftKey minecraftkey, MinecraftKey minecraftkey1, MinecraftKey minecraftkey2, String s, TileEntityJigsaw.JointType tileentityjigsaw_jointtype, int i, int j) {
        this.pos = blockposition;
        this.name = minecraftkey;
        this.target = minecraftkey1;
        this.pool = minecraftkey2;
        this.finalState = s;
        this.joint = tileentityjigsaw_jointtype;
        this.selectionPriority = i;
        this.placementPriority = j;
    }

    private PacketPlayInSetJigsaw(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.name = packetdataserializer.readResourceLocation();
        this.target = packetdataserializer.readResourceLocation();
        this.pool = packetdataserializer.readResourceLocation();
        this.finalState = packetdataserializer.readUtf();
        this.joint = (TileEntityJigsaw.JointType) TileEntityJigsaw.JointType.byName(packetdataserializer.readUtf()).orElse(TileEntityJigsaw.JointType.ALIGNED);
        this.selectionPriority = packetdataserializer.readVarInt();
        this.placementPriority = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeResourceLocation(this.name);
        packetdataserializer.writeResourceLocation(this.target);
        packetdataserializer.writeResourceLocation(this.pool);
        packetdataserializer.writeUtf(this.finalState);
        packetdataserializer.writeUtf(this.joint.getSerializedName());
        packetdataserializer.writeVarInt(this.selectionPriority);
        packetdataserializer.writeVarInt(this.placementPriority);
    }

    @Override
    public PacketType<PacketPlayInSetJigsaw> type() {
        return GamePacketTypes.SERVERBOUND_SET_JIGSAW_BLOCK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetJigsawBlock(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public MinecraftKey getName() {
        return this.name;
    }

    public MinecraftKey getTarget() {
        return this.target;
    }

    public MinecraftKey getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public TileEntityJigsaw.JointType getJoint() {
        return this.joint;
    }

    public int getSelectionPriority() {
        return this.selectionPriority;
    }

    public int getPlacementPriority() {
        return this.placementPriority;
    }
}
