package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class PacketPlayOutBlockChange implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutBlockChange> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, PacketPlayOutBlockChange::getPos, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), PacketPlayOutBlockChange::getBlockState, PacketPlayOutBlockChange::new);
    private final BlockPosition pos;
    public final IBlockData blockState;

    public PacketPlayOutBlockChange(BlockPosition blockposition, IBlockData iblockdata) {
        this.pos = blockposition;
        this.blockState = iblockdata;
    }

    public PacketPlayOutBlockChange(IBlockAccess iblockaccess, BlockPosition blockposition) {
        this(blockposition, iblockaccess.getBlockState(blockposition));
    }

    @Override
    public PacketType<PacketPlayOutBlockChange> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_UPDATE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBlockUpdate(this);
    }

    public IBlockData getBlockState() {
        return this.blockState;
    }

    public BlockPosition getPos() {
        return this.pos;
    }
}
