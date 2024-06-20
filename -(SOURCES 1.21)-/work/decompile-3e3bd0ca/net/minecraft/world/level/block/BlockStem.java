package net.minecraft.world.level.block;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockStem extends BlockPlant implements IBlockFragilePlantElement {

    public static final MapCodec<BlockStem> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter((blockstem) -> {
            return blockstem.fruit;
        }), ResourceKey.codec(Registries.BLOCK).fieldOf("attached_stem").forGetter((blockstem) -> {
            return blockstem.attachedStem;
        }), ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter((blockstem) -> {
            return blockstem.seed;
        }), propertiesCodec()).apply(instance, BlockStem::new);
    });
    public static final int MAX_AGE = 7;
    public static final BlockStateInteger AGE = BlockProperties.AGE_7;
    protected static final float AABB_OFFSET = 1.0F;
    protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
    private final ResourceKey<Block> fruit;
    private final ResourceKey<Block> attachedStem;
    private final ResourceKey<Item> seed;

    @Override
    public MapCodec<BlockStem> codec() {
        return BlockStem.CODEC;
    }

    protected BlockStem(ResourceKey<Block> resourcekey, ResourceKey<Block> resourcekey1, ResourceKey<Item> resourcekey2, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.fruit = resourcekey;
        this.attachedStem = resourcekey1;
        this.seed = resourcekey2;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockStem.AGE, 0));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockStem.SHAPE_BY_AGE[(Integer) iblockdata.getValue(BlockStem.AGE)];
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(Blocks.FARMLAND);
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (worldserver.getRawBrightness(blockposition, 0) >= 9) {
            float f = BlockCrops.getGrowthSpeed(this, worldserver, blockposition);

            if (randomsource.nextInt((int) (25.0F / f) + 1) == 0) {
                int i = (Integer) iblockdata.getValue(BlockStem.AGE);

                if (i < 7) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockStem.AGE, i + 1);
                    worldserver.setBlock(blockposition, iblockdata, 2);
                } else {
                    EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
                    BlockPosition blockposition1 = blockposition.relative(enumdirection);
                    IBlockData iblockdata1 = worldserver.getBlockState(blockposition1.below());

                    if (worldserver.getBlockState(blockposition1).isAir() && (iblockdata1.is(Blocks.FARMLAND) || iblockdata1.is(TagsBlock.DIRT))) {
                        IRegistry<Block> iregistry = worldserver.registryAccess().registryOrThrow(Registries.BLOCK);
                        Optional<Block> optional = iregistry.getOptional(this.fruit);
                        Optional<Block> optional1 = iregistry.getOptional(this.attachedStem);

                        if (optional.isPresent() && optional1.isPresent()) {
                            worldserver.setBlockAndUpdate(blockposition1, ((Block) optional.get()).defaultBlockState());
                            worldserver.setBlockAndUpdate(blockposition, (IBlockData) ((Block) optional1.get()).defaultBlockState().setValue(BlockFacingHorizontal.FACING, enumdirection));
                        }
                    }
                }
            }

        }
    }

    @Override
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack((IMaterial) DataFixUtils.orElse(iworldreader.registryAccess().registryOrThrow(Registries.ITEM).getOptional(this.seed), this));
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockStem.AGE) != 7;
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        int i = Math.min(7, (Integer) iblockdata.getValue(BlockStem.AGE) + MathHelper.nextInt(worldserver.random, 2, 5));
        IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(BlockStem.AGE, i);

        worldserver.setBlock(blockposition, iblockdata1, 2);
        if (i == 7) {
            iblockdata1.randomTick(worldserver, blockposition, worldserver.random);
        }

    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockStem.AGE);
    }
}
