package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyHalf;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockTrapdoor extends BlockFacingHorizontal implements IBlockWaterlogged {

    public static final MapCodec<BlockTrapdoor> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter((blocktrapdoor) -> {
            return blocktrapdoor.type;
        }), propertiesCodec()).apply(instance, BlockTrapdoor::new);
    });
    public static final BlockStateBoolean OPEN = BlockProperties.OPEN;
    public static final BlockStateEnum<BlockPropertyHalf> HALF = BlockProperties.HALF;
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    protected static final int AABB_THICKNESS = 3;
    protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    protected static final VoxelShape TOP_AABB = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private final BlockSetType type;

    @Override
    public MapCodec<? extends BlockTrapdoor> codec() {
        return BlockTrapdoor.CODEC;
    }

    protected BlockTrapdoor(BlockSetType blocksettype, BlockBase.Info blockbase_info) {
        super(blockbase_info.sound(blocksettype.soundType()));
        this.type = blocksettype;
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockTrapdoor.FACING, EnumDirection.NORTH)).setValue(BlockTrapdoor.OPEN, false)).setValue(BlockTrapdoor.HALF, BlockPropertyHalf.BOTTOM)).setValue(BlockTrapdoor.POWERED, false)).setValue(BlockTrapdoor.WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        if (!(Boolean) iblockdata.getValue(BlockTrapdoor.OPEN)) {
            return iblockdata.getValue(BlockTrapdoor.HALF) == BlockPropertyHalf.TOP ? BlockTrapdoor.TOP_AABB : BlockTrapdoor.BOTTOM_AABB;
        } else {
            switch ((EnumDirection) iblockdata.getValue(BlockTrapdoor.FACING)) {
                case NORTH:
                default:
                    return BlockTrapdoor.NORTH_OPEN_AABB;
                case SOUTH:
                    return BlockTrapdoor.SOUTH_OPEN_AABB;
                case WEST:
                    return BlockTrapdoor.WEST_OPEN_AABB;
                case EAST:
                    return BlockTrapdoor.EAST_OPEN_AABB;
            }
        }
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        switch (pathmode) {
            case LAND:
                return (Boolean) iblockdata.getValue(BlockTrapdoor.OPEN);
            case WATER:
                return (Boolean) iblockdata.getValue(BlockTrapdoor.WATERLOGGED);
            case AIR:
                return (Boolean) iblockdata.getValue(BlockTrapdoor.OPEN);
            default:
                return false;
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!this.type.canOpenByHand()) {
            return EnumInteractionResult.PASS;
        } else {
            this.toggle(iblockdata, world, blockposition, entityhuman);
            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    @Override
    protected void onExplosionHit(IBlockData iblockdata, World world, BlockPosition blockposition, Explosion explosion, BiConsumer<ItemStack, BlockPosition> biconsumer) {
        if (explosion.getBlockInteraction() == Explosion.Effect.TRIGGER_BLOCK && !world.isClientSide() && this.type.canOpenByWindCharge() && !(Boolean) iblockdata.getValue(BlockTrapdoor.POWERED)) {
            this.toggle(iblockdata, world, blockposition, (EntityHuman) null);
        }

        super.onExplosionHit(iblockdata, world, blockposition, explosion, biconsumer);
    }

    private void toggle(IBlockData iblockdata, World world, BlockPosition blockposition, @Nullable EntityHuman entityhuman) {
        IBlockData iblockdata1 = (IBlockData) iblockdata.cycle(BlockTrapdoor.OPEN);

        world.setBlock(blockposition, iblockdata1, 2);
        if ((Boolean) iblockdata1.getValue(BlockTrapdoor.WATERLOGGED)) {
            world.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(world));
        }

        this.playSound(entityhuman, world, blockposition, (Boolean) iblockdata1.getValue(BlockTrapdoor.OPEN));
    }

    protected void playSound(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition, boolean flag) {
        world.playSound(entityhuman, blockposition, flag ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
        world.gameEvent((Entity) entityhuman, (Holder) (flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE), blockposition);
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        if (!world.isClientSide) {
            boolean flag1 = world.hasNeighborSignal(blockposition);

            if (flag1 != (Boolean) iblockdata.getValue(BlockTrapdoor.POWERED)) {
                if ((Boolean) iblockdata.getValue(BlockTrapdoor.OPEN) != flag1) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockTrapdoor.OPEN, flag1);
                    this.playSound((EntityHuman) null, world, blockposition, flag1);
                }

                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockTrapdoor.POWERED, flag1), 2);
                if ((Boolean) iblockdata.getValue(BlockTrapdoor.WATERLOGGED)) {
                    world.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(world));
                }
            }

        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.defaultBlockState();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        EnumDirection enumdirection = blockactioncontext.getClickedFace();

        if (!blockactioncontext.replacingClickedOnBlock() && enumdirection.getAxis().isHorizontal()) {
            iblockdata = (IBlockData) ((IBlockData) iblockdata.setValue(BlockTrapdoor.FACING, enumdirection)).setValue(BlockTrapdoor.HALF, blockactioncontext.getClickLocation().y - (double) blockactioncontext.getClickedPos().getY() > 0.5D ? BlockPropertyHalf.TOP : BlockPropertyHalf.BOTTOM);
        } else {
            iblockdata = (IBlockData) ((IBlockData) iblockdata.setValue(BlockTrapdoor.FACING, blockactioncontext.getHorizontalDirection().getOpposite())).setValue(BlockTrapdoor.HALF, enumdirection == EnumDirection.UP ? BlockPropertyHalf.BOTTOM : BlockPropertyHalf.TOP);
        }

        if (blockactioncontext.getLevel().hasNeighborSignal(blockactioncontext.getClickedPos())) {
            iblockdata = (IBlockData) ((IBlockData) iblockdata.setValue(BlockTrapdoor.OPEN, true)).setValue(BlockTrapdoor.POWERED, true);
        }

        return (IBlockData) iblockdata.setValue(BlockTrapdoor.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockTrapdoor.FACING, BlockTrapdoor.OPEN, BlockTrapdoor.HALF, BlockTrapdoor.POWERED, BlockTrapdoor.WATERLOGGED);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockTrapdoor.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(BlockTrapdoor.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    protected BlockSetType getType() {
        return this.type;
    }
}
