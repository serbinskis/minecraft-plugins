package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockHopper extends BlockTileEntity {

    public static final MapCodec<BlockHopper> CODEC = simpleCodec(BlockHopper::new);
    public static final BlockStateDirection FACING = BlockProperties.FACING_HOPPER;
    public static final BlockStateBoolean ENABLED = BlockProperties.ENABLED;
    private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape FUNNEL = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape CONVEX_BASE = VoxelShapes.or(BlockHopper.FUNNEL, BlockHopper.TOP);
    private static final VoxelShape INSIDE = box(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape BASE = VoxelShapes.join(BlockHopper.CONVEX_BASE, BlockHopper.INSIDE, OperatorBoolean.ONLY_FIRST);
    private static final VoxelShape DOWN_SHAPE = VoxelShapes.or(BlockHopper.BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
    private static final VoxelShape EAST_SHAPE = VoxelShapes.or(BlockHopper.BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(BlockHopper.BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
    private static final VoxelShape SOUTH_SHAPE = VoxelShapes.or(BlockHopper.BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
    private static final VoxelShape WEST_SHAPE = VoxelShapes.or(BlockHopper.BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
    private static final VoxelShape DOWN_INTERACTION_SHAPE = BlockHopper.INSIDE;
    private static final VoxelShape EAST_INTERACTION_SHAPE = VoxelShapes.or(BlockHopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
    private static final VoxelShape NORTH_INTERACTION_SHAPE = VoxelShapes.or(BlockHopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
    private static final VoxelShape SOUTH_INTERACTION_SHAPE = VoxelShapes.or(BlockHopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
    private static final VoxelShape WEST_INTERACTION_SHAPE = VoxelShapes.or(BlockHopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));

    @Override
    public MapCodec<BlockHopper> codec() {
        return BlockHopper.CODEC;
    }

    public BlockHopper(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockHopper.FACING, EnumDirection.DOWN)).setValue(BlockHopper.ENABLED, true));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        switch ((EnumDirection) iblockdata.getValue(BlockHopper.FACING)) {
            case DOWN:
                return BlockHopper.DOWN_SHAPE;
            case NORTH:
                return BlockHopper.NORTH_SHAPE;
            case SOUTH:
                return BlockHopper.SOUTH_SHAPE;
            case WEST:
                return BlockHopper.WEST_SHAPE;
            case EAST:
                return BlockHopper.EAST_SHAPE;
            default:
                return BlockHopper.BASE;
        }
    }

    @Override
    protected VoxelShape getInteractionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        switch ((EnumDirection) iblockdata.getValue(BlockHopper.FACING)) {
            case DOWN:
                return BlockHopper.DOWN_INTERACTION_SHAPE;
            case NORTH:
                return BlockHopper.NORTH_INTERACTION_SHAPE;
            case SOUTH:
                return BlockHopper.SOUTH_INTERACTION_SHAPE;
            case WEST:
                return BlockHopper.WEST_INTERACTION_SHAPE;
            case EAST:
                return BlockHopper.EAST_INTERACTION_SHAPE;
            default:
                return BlockHopper.INSIDE;
        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getClickedFace().getOpposite();

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockHopper.FACING, enumdirection.getAxis() == EnumDirection.EnumAxis.Y ? EnumDirection.DOWN : enumdirection)).setValue(BlockHopper.ENABLED, true);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityHopper(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, TileEntityTypes.HOPPER, TileEntityHopper::pushItemsTick);
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            this.checkPoweredState(world, blockposition, iblockdata);
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityHopper) {
                entityhuman.openMenu((TileEntityHopper) tileentity);
                entityhuman.awardStat(StatisticList.INSPECT_HOPPER);
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        this.checkPoweredState(world, blockposition, iblockdata);
    }

    private void checkPoweredState(World world, BlockPosition blockposition, IBlockData iblockdata) {
        boolean flag = !world.hasNeighborSignal(blockposition);

        if (flag != (Boolean) iblockdata.getValue(BlockHopper.ENABLED)) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockHopper.ENABLED, flag), 2);
        }

    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        InventoryUtils.dropContentsOnDestroy(iblockdata, iblockdata1, world, blockposition);
        super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockHopper.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockHopper.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockHopper.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockHopper.FACING, BlockHopper.ENABLED);
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityHopper) {
            TileEntityHopper.entityInside(world, blockposition, iblockdata, entity, (TileEntityHopper) tileentity);
        }

    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
