package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlock extends SculkSensorBlock {

    public static final BlockStateDirection FACING = BlockProperties.HORIZONTAL_FACING;

    public CalibratedSculkSensorBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(CalibratedSculkSensorBlock.FACING, EnumDirection.NORTH));
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new CalibratedSculkSensorBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return !world.isClientSide ? createTickerHelper(tileentitytypes, TileEntityTypes.CALIBRATED_SCULK_SENSOR, (world1, blockposition, iblockdata1, calibratedsculksensorblockentity) -> {
            VibrationSystem.c.tick(world1, calibratedsculksensorblockentity.getVibrationData(), calibratedsculksensorblockentity.getVibrationUser());
        }) : null;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) super.getStateForPlacement(blockactioncontext).setValue(CalibratedSculkSensorBlock.FACING, blockactioncontext.getHorizontalDirection());
    }

    @Override
    public int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection != iblockdata.getValue(CalibratedSculkSensorBlock.FACING) ? super.getSignal(iblockdata, iblockaccess, blockposition, enumdirection) : 0;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        super.createBlockStateDefinition(blockstatelist_a);
        blockstatelist_a.add(CalibratedSculkSensorBlock.FACING);
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(CalibratedSculkSensorBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(CalibratedSculkSensorBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(CalibratedSculkSensorBlock.FACING)));
    }

    @Override
    public int getActiveTicks() {
        return 10;
    }
}
