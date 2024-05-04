package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBrewingStand;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockBrewingStand extends BlockTileEntity {

    public static final MapCodec<BlockBrewingStand> CODEC = simpleCodec(BlockBrewingStand::new);
    public static final BlockStateBoolean[] HAS_BOTTLE = new BlockStateBoolean[]{BlockProperties.HAS_BOTTLE_0, BlockProperties.HAS_BOTTLE_1, BlockProperties.HAS_BOTTLE_2};
    protected static final VoxelShape SHAPE = VoxelShapes.or(Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D));

    @Override
    public MapCodec<BlockBrewingStand> codec() {
        return BlockBrewingStand.CODEC;
    }

    public BlockBrewingStand(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockBrewingStand.HAS_BOTTLE[0], false)).setValue(BlockBrewingStand.HAS_BOTTLE[1], false)).setValue(BlockBrewingStand.HAS_BOTTLE[2], false));
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityBrewingStand(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, TileEntityTypes.BREWING_STAND, TileEntityBrewingStand::serverTick);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockBrewingStand.SHAPE;
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityBrewingStand) {
                entityhuman.openMenu((TileEntityBrewingStand) tileentity);
                entityhuman.awardStat(StatisticList.INTERACT_WITH_BREWINGSTAND);
            }

            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX() + 0.4D + (double) randomsource.nextFloat() * 0.2D;
        double d1 = (double) blockposition.getY() + 0.7D + (double) randomsource.nextFloat() * 0.3D;
        double d2 = (double) blockposition.getZ() + 0.4D + (double) randomsource.nextFloat() * 0.2D;

        world.addParticle(Particles.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        InventoryUtils.dropContentsOnDestroy(iblockdata, iblockdata1, world, blockposition);
        super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
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
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBrewingStand.HAS_BOTTLE[0], BlockBrewingStand.HAS_BOTTLE[1], BlockBrewingStand.HAS_BOTTLE[2]);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
