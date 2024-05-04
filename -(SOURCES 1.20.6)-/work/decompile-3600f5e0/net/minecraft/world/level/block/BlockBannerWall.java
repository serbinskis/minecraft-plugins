package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockBannerWall extends BlockBannerAbstract {

    public static final MapCodec<BlockBannerWall> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnumColor.CODEC.fieldOf("color").forGetter(BlockBannerAbstract::getColor), propertiesCodec()).apply(instance, BlockBannerWall::new);
    });
    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    private static final Map<EnumDirection, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(EnumDirection.NORTH, Block.box(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D), EnumDirection.SOUTH, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D), EnumDirection.WEST, Block.box(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D), EnumDirection.EAST, Block.box(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));

    @Override
    public MapCodec<BlockBannerWall> codec() {
        return BlockBannerWall.CODEC;
    }

    public BlockBannerWall(EnumColor enumcolor, BlockBase.Info blockbase_info) {
        super(enumcolor, blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockBannerWall.FACING, EnumDirection.NORTH));
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition.relative(((EnumDirection) iblockdata.getValue(BlockBannerWall.FACING)).getOpposite())).isSolid();
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection == ((EnumDirection) iblockdata.getValue(BlockBannerWall.FACING)).getOpposite() && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockBannerWall.SHAPES.get(iblockdata.getValue(BlockBannerWall.FACING));
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.defaultBlockState();
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();
        EnumDirection[] aenumdirection1 = aenumdirection;
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection1[j];

            if (enumdirection.getAxis().isHorizontal()) {
                EnumDirection enumdirection1 = enumdirection.getOpposite();

                iblockdata = (IBlockData) iblockdata.setValue(BlockBannerWall.FACING, enumdirection1);
                if (iblockdata.canSurvive(world, blockposition)) {
                    return iblockdata;
                }
            }
        }

        return null;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockBannerWall.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockBannerWall.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockBannerWall.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBannerWall.FACING);
    }
}
