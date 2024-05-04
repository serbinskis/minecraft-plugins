package net.minecraft.network.protocol.game;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;

public class PacketPlayOutTileEntityData implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutTileEntityData> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, PacketPlayOutTileEntityData::getPos, ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE), PacketPlayOutTileEntityData::getType, ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketPlayOutTileEntityData::getTag, PacketPlayOutTileEntityData::new);
    private final BlockPosition pos;
    private final TileEntityTypes<?> type;
    private final NBTTagCompound tag;

    public static PacketPlayOutTileEntityData create(TileEntity tileentity, BiFunction<TileEntity, IRegistryCustom, NBTTagCompound> bifunction) {
        IRegistryCustom iregistrycustom = tileentity.getLevel().registryAccess();

        return new PacketPlayOutTileEntityData(tileentity.getBlockPos(), tileentity.getType(), (NBTTagCompound) bifunction.apply(tileentity, iregistrycustom));
    }

    public static PacketPlayOutTileEntityData create(TileEntity tileentity) {
        return create(tileentity, TileEntity::getUpdateTag);
    }

    public PacketPlayOutTileEntityData(BlockPosition blockposition, TileEntityTypes<?> tileentitytypes, NBTTagCompound nbttagcompound) {
        this.pos = blockposition;
        this.type = tileentitytypes;
        this.tag = nbttagcompound;
    }

    @Override
    public PacketType<PacketPlayOutTileEntityData> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBlockEntityData(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public TileEntityTypes<?> getType() {
        return this.type;
    }

    public NBTTagCompound getTag() {
        return this.tag;
    }
}
