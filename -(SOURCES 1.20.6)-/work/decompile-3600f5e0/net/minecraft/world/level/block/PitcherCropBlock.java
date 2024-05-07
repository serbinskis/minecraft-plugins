package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.monster.EntityRavager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class PitcherCropBlock extends BlockTallPlant implements IBlockFragilePlantElement {

    public static final MapCodec<PitcherCropBlock> CODEC = simpleCodec(PitcherCropBlock::new);
    public static final BlockStateInteger AGE = BlockProperties.AGE_4;
    public static final int MAX_AGE = 4;
    private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
    private static final int BONEMEAL_INCREASE = 1;
    private static final VoxelShape FULL_UPPER_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);
    private static final VoxelShape FULL_LOWER_SHAPE = Block.box(3.0D, -1.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0D, -1.0D, 5.0D, 11.0D, 3.0D, 11.0D);
    private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0D, -1.0D, 3.0D, 13.0D, 5.0D, 13.0D);
    private static final VoxelShape[] UPPER_SHAPE_BY_AGE = new VoxelShape[]{Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D), PitcherCropBlock.FULL_UPPER_SHAPE};
    private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{PitcherCropBlock.COLLISION_SHAPE_BULB, Block.box(3.0D, -1.0D, 3.0D, 13.0D, 14.0D, 13.0D), PitcherCropBlock.FULL_LOWER_SHAPE, PitcherCropBlock.FULL_LOWER_SHAPE, PitcherCropBlock.FULL_LOWER_SHAPE};

    @Override
    public MapCodec<PitcherCropBlock> codec() {
        return PitcherCropBlock.CODEC;
    }

    public PitcherCropBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.UPPER ? PitcherCropBlock.UPPER_SHAPE_BY_AGE[Math.min(Math.abs(4 - ((Integer) iblockdata.getValue(PitcherCropBlock.AGE) + 1)), PitcherCropBlock.UPPER_SHAPE_BY_AGE.length - 1)] : PitcherCropBlock.LOWER_SHAPE_BY_AGE[(Integer) iblockdata.getValue(PitcherCropBlock.AGE)];
    }

    @Override
    public VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (Integer) iblockdata.getValue(PitcherCropBlock.AGE) == 0 ? PitcherCropBlock.COLLISION_SHAPE_BULB : (iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER ? PitcherCropBlock.COLLISION_SHAPE_CROP : super.getCollisionShape(iblockdata, iblockaccess, blockposition, voxelshapecollision));
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return isDouble((Integer) iblockdata.getValue(PitcherCropBlock.AGE)) ? super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1) : (iblockdata.canSurvive(generatoraccess, blockposition) ? iblockdata : Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return isLower(iblockdata) && !sufficientLight(iworldreader, blockposition) ? false : super.canSurvive(iblockdata, iworldreader, blockposition);
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(Blocks.FARMLAND);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(PitcherCropBlock.AGE);
        super.createBlockStateDefinition(blockstatelist_a);
    }

    @Override
    public void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (entity instanceof EntityRavager && world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            world.destroyBlock(blockposition, true, entity);
        }

        super.entityInside(iblockdata, world, blockposition, entity);
    }

    @Override
    public boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return false;
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {}

    @Override
    public boolean isRandomlyTicking(IBlockData iblockdata) {
        return iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER && !this.isMaxAge(iblockdata);
    }

    @Override
    public void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        float f = BlockCrops.getGrowthSpeed(this, worldserver, blockposition);
        boolean flag = randomsource.nextInt((int) (25.0F / f) + 1) == 0;

        if (flag) {
            this.grow(worldserver, iblockdata, blockposition, 1);
        }

    }

    private void grow(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition, int i) {
        int j = Math.min((Integer) iblockdata.getValue(PitcherCropBlock.AGE) + i, 4);

        if (this.canGrow(worldserver, blockposition, iblockdata, j)) {
            IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(PitcherCropBlock.AGE, j);

            worldserver.setBlock(blockposition, iblockdata1, 2);
            if (isDouble(j)) {
                worldserver.setBlock(blockposition.above(), (IBlockData) iblockdata1.setValue(PitcherCropBlock.HALF, BlockPropertyDoubleBlockHalf.UPPER), 3);
            }

        }
    }

    private static boolean canGrowInto(IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata = iworldreader.getBlockState(blockposition);

        return iblockdata.isAir() || iblockdata.is(Blocks.PITCHER_CROP);
    }

    private static boolean sufficientLight(IWorldReader iworldreader, BlockPosition blockposition) {
        return BlockCrops.hasSufficientLight(iworldreader, blockposition);
    }

    private static boolean isLower(IBlockData iblockdata) {
        return iblockdata.is(Blocks.PITCHER_CROP) && iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER;
    }

    private static boolean isDouble(int i) {
        return i >= 3;
    }

    private boolean canGrow(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, int i) {
        return !this.isMaxAge(iblockdata) && sufficientLight(iworldreader, blockposition) && (!isDouble(i) || canGrowInto(iworldreader, blockposition.above()));
    }

    private boolean isMaxAge(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(PitcherCropBlock.AGE) >= 4;
    }

    @Nullable
    private PitcherCropBlock.a getLowerHalf(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        if (isLower(iblockdata)) {
            return new PitcherCropBlock.a(blockposition, iblockdata);
        } else {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

            return isLower(iblockdata1) ? new PitcherCropBlock.a(blockposition1, iblockdata1) : null;
        }
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        PitcherCropBlock.a pitchercropblock_a = this.getLowerHalf(iworldreader, blockposition, iblockdata);

        return pitchercropblock_a == null ? false : this.canGrow(iworldreader, pitchercropblock_a.pos, pitchercropblock_a.state, (Integer) pitchercropblock_a.state.getValue(PitcherCropBlock.AGE) + 1);
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        PitcherCropBlock.a pitchercropblock_a = this.getLowerHalf(worldserver, blockposition, iblockdata);

        if (pitchercropblock_a != null) {
            this.grow(worldserver, pitchercropblock_a.state, pitchercropblock_a.pos, 1);
        }
    }

    private static record a(BlockPosition pos, IBlockData state) {

    }
}
