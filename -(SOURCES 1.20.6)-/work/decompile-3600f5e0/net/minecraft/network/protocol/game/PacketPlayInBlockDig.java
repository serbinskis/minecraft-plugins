package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInBlockDig implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInBlockDig> STREAM_CODEC = Packet.codec(PacketPlayInBlockDig::write, PacketPlayInBlockDig::new);
    private final BlockPosition pos;
    private final EnumDirection direction;
    private final PacketPlayInBlockDig.EnumPlayerDigType action;
    private final int sequence;

    public PacketPlayInBlockDig(PacketPlayInBlockDig.EnumPlayerDigType packetplayinblockdig_enumplayerdigtype, BlockPosition blockposition, EnumDirection enumdirection, int i) {
        this.action = packetplayinblockdig_enumplayerdigtype;
        this.pos = blockposition.immutable();
        this.direction = enumdirection;
        this.sequence = i;
    }

    public PacketPlayInBlockDig(PacketPlayInBlockDig.EnumPlayerDigType packetplayinblockdig_enumplayerdigtype, BlockPosition blockposition, EnumDirection enumdirection) {
        this(packetplayinblockdig_enumplayerdigtype, blockposition, enumdirection, 0);
    }

    private PacketPlayInBlockDig(PacketDataSerializer packetdataserializer) {
        this.action = (PacketPlayInBlockDig.EnumPlayerDigType) packetdataserializer.readEnum(PacketPlayInBlockDig.EnumPlayerDigType.class);
        this.pos = packetdataserializer.readBlockPos();
        this.direction = EnumDirection.from3DDataValue(packetdataserializer.readUnsignedByte());
        this.sequence = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.action);
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeByte(this.direction.get3DDataValue());
        packetdataserializer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<PacketPlayInBlockDig> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_ACTION;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePlayerAction(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public EnumDirection getDirection() {
        return this.direction;
    }

    public PacketPlayInBlockDig.EnumPlayerDigType getAction() {
        return this.action;
    }

    public int getSequence() {
        return this.sequence;
    }

    public static enum EnumPlayerDigType {

        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_ITEM_WITH_OFFHAND;

        private EnumPlayerDigType() {}
    }
}
