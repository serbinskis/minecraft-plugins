package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class AmethystClusterBlock extends AmethystBlock implements IBlockWaterlogged {

    public static final MapCodec<AmethystClusterBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("height").forGetter((amethystclusterblock) -> {
            return amethystclusterblock.height;
        }), Codec.FLOAT.fieldOf("aabb_offset").forGetter((amethystclusterblock) -> {
            return amethystclusterblock.aabbOffset;
        }), propertiesCodec()).apply(instance, AmethystClusterBlock::new);
    });
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    public static final BlockStateDirection FACING = BlockProperties.FACING;
    private final float height;
    private final float aabbOffset;
    protected final VoxelShape northAabb;
    protected final VoxelShape southAabb;
    protected final VoxelShape eastAabb;
    protected final VoxelShape westAabb;
    protected final VoxelShape upAabb;
    protected final VoxelShape downAabb;

    @Override
    public MapCodec<AmethystClusterBlock> codec() {
        return AmethystClusterBlock.CODEC;
    }

    public AmethystClusterBlock(float f, float f1, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.defaultBlockState().setValue(AmethystClusterBlock.WATERLOGGED, false)).setValue(AmethystClusterBlock.FACING, EnumDirection.UP));
        this.upAabb = Block.box((double) f1, 0.0D, (double) f1, (double) (16.0F - f1), (double) f, (double) (16.0F - f1));
        this.downAabb = Block.box((double) f1, (double) (16.0F - f), (double) f1, (double) (16.0F - f1), 16.0D, (double) (16.0F - f1));
        this.northAabb = Block.box((double) f1, (double) f1, (double) (16.0F - f), (double) (16.0F - f1), (double) (16.0F - f1), 16.0D);
        this.southAabb = Block.box((double) f1, (double) f1, 0.0D, (double) (16.0F - f1), (double) (16.0F - f1), (double) f);
        this.eastAabb = Block.box(0.0D, (double) f1, (double) f1, (double) f, (double) (16.0F - f1), (double) (16.0F - f1));
        this.westAabb = Block.box((double) (16.0F - f), (double) f1, (double) f1, 16.0D, (double) (16.0F - f1), (double) (16.0F - f1));
        this.height = f;
        this.aabbOffset = f1;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING);

        switch (enumdirection) {
            case NORTH:
                return this.northAabb;
            case SOUTH:
                return this.southAabb;
            case EAST:
                return this.eastAabb;
            case WEST:
                return this.westAabb;
            case DOWN:
                return this.downAabb;
            case UP:
            default:
                return this.upAabb;
        }
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING);
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());

        return iworldreader.getBlockState(blockposition1).isFaceSturdy(iworldreader, blockposition1, enumdirection);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(AmethystClusterBlock.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return enumdirection == ((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)).getOpposite() && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(AmethystClusterBlock.WATERLOGGED, world.getFluidState(blockposition).getType() == FluidTypes.WATER)).setValue(AmethystClusterBlock.FACING, blockactioncontext.getClickedFace());
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(AmethystClusterBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)));
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(AmethystClusterBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(AmethystClusterBlock.WATERLOGGED, AmethystClusterBlock.FACING);
    }
}
