package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class CandleCakeBlock extends AbstractCandleBlock {

    public static final MapCodec<CandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter((candlecakeblock) -> {
            return candlecakeblock.candleBlock;
        }), propertiesCodec()).apply(instance, CandleCakeBlock::new);
    });
    public static final BlockStateBoolean LIT = AbstractCandleBlock.LIT;
    protected static final float AABB_OFFSET = 1.0F;
    protected static final VoxelShape CAKE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);
    protected static final VoxelShape CANDLE_SHAPE = Block.box(7.0D, 8.0D, 7.0D, 9.0D, 14.0D, 9.0D);
    protected static final VoxelShape SHAPE = VoxelShapes.or(CandleCakeBlock.CAKE_SHAPE, CandleCakeBlock.CANDLE_SHAPE);
    private static final Map<CandleBlock, CandleCakeBlock> BY_CANDLE = Maps.newHashMap();
    private static final Iterable<Vec3D> PARTICLE_OFFSETS = ImmutableList.of(new Vec3D(0.5D, 1.0D, 0.5D));
    private final CandleBlock candleBlock;

    @Override
    public MapCodec<CandleCakeBlock> codec() {
        return CandleCakeBlock.CODEC;
    }

    protected CandleCakeBlock(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(CandleCakeBlock.LIT, false));
        if (block instanceof CandleBlock candleblock) {
            CandleCakeBlock.BY_CANDLE.put(candleblock, this);
            this.candleBlock = candleblock;
        } else {
            String s = String.valueOf(CandleBlock.class);

            throw new IllegalArgumentException("Expected block to be of " + s + " was " + String.valueOf(block.getClass()));
        }
    }

    @Override
    protected Iterable<Vec3D> getParticleOffsets(IBlockData iblockdata) {
        return CandleCakeBlock.PARTICLE_OFFSETS;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return CandleCakeBlock.SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
            if (candleHit(movingobjectpositionblock) && itemstack.isEmpty() && (Boolean) iblockdata.getValue(CandleCakeBlock.LIT)) {
                extinguish(entityhuman, iblockdata, world, blockposition);
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
            }
        } else {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        EnumInteractionResult enuminteractionresult = BlockCake.eat(world, blockposition, Blocks.CAKE.defaultBlockState(), entityhuman);

        if (enuminteractionresult.consumesAction()) {
            dropResources(iblockdata, world, blockposition);
        }

        return enuminteractionresult;
    }

    private static boolean candleHit(MovingObjectPositionBlock movingobjectpositionblock) {
        return movingobjectpositionblock.getLocation().y - (double) movingobjectpositionblock.getBlockPos().getY() > 0.5D;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CandleCakeBlock.LIT);
    }

    @Override
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack(Blocks.CAKE);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition.below()).isSolid();
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return BlockCake.FULL_CAKE_SIGNAL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    public static IBlockData byCandle(CandleBlock candleblock) {
        return ((CandleCakeBlock) CandleCakeBlock.BY_CANDLE.get(candleblock)).defaultBlockState();
    }

    public static boolean canLight(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.CANDLE_CAKES, (blockbase_blockdata) -> {
            return blockbase_blockdata.hasProperty(CandleCakeBlock.LIT) && !(Boolean) iblockdata.getValue(CandleCakeBlock.LIT);
        });
    }
}
