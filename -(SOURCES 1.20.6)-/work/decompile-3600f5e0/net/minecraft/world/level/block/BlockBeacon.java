package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeacon;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockBeacon extends BlockTileEntity implements IBeaconBeam {

    public static final MapCodec<BlockBeacon> CODEC = simpleCodec(BlockBeacon::new);

    @Override
    public MapCodec<BlockBeacon> codec() {
        return BlockBeacon.CODEC;
    }

    public BlockBeacon(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public EnumColor getColor() {
        return EnumColor.WHITE;
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityBeacon(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.BEACON, TileEntityBeacon::tick);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityBeacon) {
                TileEntityBeacon tileentitybeacon = (TileEntityBeacon) tileentity;

                entityhuman.openMenu(tileentitybeacon);
                entityhuman.awardStat(StatisticList.INTERACT_WITH_BEACON);
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }
}
