package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyWood;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class CeilingHangingSignBlock extends BlockSign {

    public static final MapCodec<CeilingHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockPropertyWood.CODEC.fieldOf("wood_type").forGetter(BlockSign::type), propertiesCodec()).apply(instance, CeilingHangingSignBlock::new);
    });
    public static final BlockStateInteger ROTATION = BlockProperties.ROTATION_16;
    public static final BlockStateBoolean ATTACHED = BlockProperties.ATTACHED;
    protected static final float AABB_OFFSET = 5.0F;
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final Map<Integer, VoxelShape> AABBS = Maps.newHashMap(ImmutableMap.of(0, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 4, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D), 8, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 12, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D)));

    @Override
    public MapCodec<CeilingHangingSignBlock> codec() {
        return CeilingHangingSignBlock.CODEC;
    }

    public CeilingHangingSignBlock(BlockPropertyWood blockpropertywood, BlockBase.Info blockbase_info) {
        super(blockpropertywood, blockbase_info.sound(blockpropertywood.hangingSignSoundType()));
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(CeilingHangingSignBlock.ROTATION, 0)).setValue(CeilingHangingSignBlock.ATTACHED, false)).setValue(CeilingHangingSignBlock.WATERLOGGED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntitySign tileentitysign) {
            if (this.shouldTryToChainAnotherHangingSign(entityhuman, movingobjectpositionblock, tileentitysign, itemstack)) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
        }

        return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
    }

    private boolean shouldTryToChainAnotherHangingSign(EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock, TileEntitySign tileentitysign, ItemStack itemstack) {
        return !tileentitysign.canExecuteClickCommands(tileentitysign.isFacingFrontText(entityhuman), entityhuman) && itemstack.getItem() instanceof HangingSignItem && movingobjectpositionblock.getDirection().equals(EnumDirection.DOWN);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition.above()).isFaceSturdy(iworldreader, blockposition.above(), EnumDirection.DOWN, EnumBlockSupport.CENTER);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        World world = blockactioncontext.getLevel();
        Fluid fluid = world.getFluidState(blockactioncontext.getClickedPos());
        BlockPosition blockposition = blockactioncontext.getClickedPos().above();
        IBlockData iblockdata = world.getBlockState(blockposition);
        boolean flag = iblockdata.is(TagsBlock.ALL_HANGING_SIGNS);
        EnumDirection enumdirection = EnumDirection.fromYRot((double) blockactioncontext.getRotation());
        boolean flag1 = !Block.isFaceFull(iblockdata.getCollisionShape(world, blockposition), EnumDirection.DOWN) || blockactioncontext.isSecondaryUseActive();

        if (flag && !blockactioncontext.isSecondaryUseActive()) {
            if (iblockdata.hasProperty(WallHangingSignBlock.FACING)) {
                EnumDirection enumdirection1 = (EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING);

                if (enumdirection1.getAxis().test(enumdirection)) {
                    flag1 = false;
                }
            } else if (iblockdata.hasProperty(CeilingHangingSignBlock.ROTATION)) {
                Optional<EnumDirection> optional = RotationSegment.convertToDirection((Integer) iblockdata.getValue(CeilingHangingSignBlock.ROTATION));

                if (optional.isPresent() && ((EnumDirection) optional.get()).getAxis().test(enumdirection)) {
                    flag1 = false;
                }
            }
        }

        int i = !flag1 ? RotationSegment.convertToSegment(enumdirection.getOpposite()) : RotationSegment.convertToSegment(blockactioncontext.getRotation() + 180.0F);

        return (IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(CeilingHangingSignBlock.ATTACHED, flag1)).setValue(CeilingHangingSignBlock.ROTATION, i)).setValue(CeilingHangingSignBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        VoxelShape voxelshape = (VoxelShape) CeilingHangingSignBlock.AABBS.get(iblockdata.getValue(CeilingHangingSignBlock.ROTATION));

        return voxelshape == null ? CeilingHangingSignBlock.SHAPE : voxelshape;
    }

    @Override
    protected VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getShape(iblockdata, iblockaccess, blockposition, VoxelShapeCollision.empty());
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection == EnumDirection.UP && !this.canSurvive(iblockdata, generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public float getYRotationDegrees(IBlockData iblockdata) {
        return RotationSegment.convertToDegrees((Integer) iblockdata.getValue(CeilingHangingSignBlock.ROTATION));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(CeilingHangingSignBlock.ROTATION, enumblockrotation.rotate((Integer) iblockdata.getValue(CeilingHangingSignBlock.ROTATION), 16));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(CeilingHangingSignBlock.ROTATION, enumblockmirror.mirror((Integer) iblockdata.getValue(CeilingHangingSignBlock.ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CeilingHangingSignBlock.ROTATION, CeilingHangingSignBlock.ATTACHED, CeilingHangingSignBlock.WATERLOGGED);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new HangingSignBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return null; // Craftbukkit - remove unnecessary sign ticking
    }
}
