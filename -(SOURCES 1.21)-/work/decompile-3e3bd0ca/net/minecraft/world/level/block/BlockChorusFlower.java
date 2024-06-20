package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockChorusFlower extends Block {

    public static final MapCodec<BlockChorusFlower> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter((blockchorusflower) -> {
            return blockchorusflower.plant;
        }), propertiesCodec()).apply(instance, BlockChorusFlower::new);
    });
    public static final int DEAD_AGE = 5;
    public static final BlockStateInteger AGE = BlockProperties.AGE_5;
    protected static final VoxelShape BLOCK_SUPPORT_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    private final Block plant;

    @Override
    public MapCodec<BlockChorusFlower> codec() {
        return BlockChorusFlower.CODEC;
    }

    protected BlockChorusFlower(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.plant = block;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockChorusFlower.AGE, 0));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockChorusFlower.AGE) < 5;
    }

    @Override
    public VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockChorusFlower.BLOCK_SUPPORT_SHAPE;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        BlockPosition blockposition1 = blockposition.above();

        if (worldserver.isEmptyBlock(blockposition1) && blockposition1.getY() < worldserver.getMaxBuildHeight()) {
            int i = (Integer) iblockdata.getValue(BlockChorusFlower.AGE);

            if (i < 5) {
                boolean flag = false;
                boolean flag1 = false;
                IBlockData iblockdata1 = worldserver.getBlockState(blockposition.below());
                int j;

                if (iblockdata1.is(Blocks.END_STONE)) {
                    flag = true;
                } else if (iblockdata1.is(this.plant)) {
                    j = 1;

                    for (int k = 0; k < 4; ++k) {
                        IBlockData iblockdata2 = worldserver.getBlockState(blockposition.below(j + 1));

                        if (!iblockdata2.is(this.plant)) {
                            if (iblockdata2.is(Blocks.END_STONE)) {
                                flag1 = true;
                            }
                            break;
                        }

                        ++j;
                    }

                    if (j < 2 || j <= randomsource.nextInt(flag1 ? 5 : 4)) {
                        flag = true;
                    }
                } else if (iblockdata1.isAir()) {
                    flag = true;
                }

                if (flag && allNeighborsEmpty(worldserver, blockposition1, (EnumDirection) null) && worldserver.isEmptyBlock(blockposition.above(2))) {
                    worldserver.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(worldserver, blockposition, this.plant.defaultBlockState()), 2);
                    this.placeGrownFlower(worldserver, blockposition1, i);
                } else if (i < 4) {
                    j = randomsource.nextInt(4);
                    if (flag1) {
                        ++j;
                    }

                    boolean flag2 = false;

                    for (int l = 0; l < j; ++l) {
                        EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
                        BlockPosition blockposition2 = blockposition.relative(enumdirection);

                        if (worldserver.isEmptyBlock(blockposition2) && worldserver.isEmptyBlock(blockposition2.below()) && allNeighborsEmpty(worldserver, blockposition2, enumdirection.getOpposite())) {
                            this.placeGrownFlower(worldserver, blockposition2, i + 1);
                            flag2 = true;
                        }
                    }

                    if (flag2) {
                        worldserver.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(worldserver, blockposition, this.plant.defaultBlockState()), 2);
                    } else {
                        this.placeDeadFlower(worldserver, blockposition);
                    }
                } else {
                    this.placeDeadFlower(worldserver, blockposition);
                }

            }
        }
    }

    private void placeGrownFlower(World world, BlockPosition blockposition, int i) {
        world.setBlock(blockposition, (IBlockData) this.defaultBlockState().setValue(BlockChorusFlower.AGE, i), 2);
        world.levelEvent(1033, blockposition, 0);
    }

    private void placeDeadFlower(World world, BlockPosition blockposition) {
        world.setBlock(blockposition, (IBlockData) this.defaultBlockState().setValue(BlockChorusFlower.AGE, 5), 2);
        world.levelEvent(1034, blockposition, 0);
    }

    private static boolean allNeighborsEmpty(IWorldReader iworldreader, BlockPosition blockposition, @Nullable EnumDirection enumdirection) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        EnumDirection enumdirection1;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            enumdirection1 = (EnumDirection) iterator.next();
        } while (enumdirection1 == enumdirection || iworldreader.isEmptyBlock(blockposition.relative(enumdirection1)));

        return false;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (enumdirection != EnumDirection.UP && !iblockdata.canSurvive(generatoraccess, blockposition)) {
            generatoraccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

        if (!iblockdata1.is(this.plant) && !iblockdata1.is(Blocks.END_STONE)) {
            if (!iblockdata1.isAir()) {
                return false;
            } else {
                boolean flag = false;
                Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                while (iterator.hasNext()) {
                    EnumDirection enumdirection = (EnumDirection) iterator.next();
                    IBlockData iblockdata2 = iworldreader.getBlockState(blockposition.relative(enumdirection));

                    if (iblockdata2.is(this.plant)) {
                        if (flag) {
                            return false;
                        }

                        flag = true;
                    } else if (!iblockdata2.isAir()) {
                        return false;
                    }
                }

                return flag;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockChorusFlower.AGE);
    }

    public static void generatePlant(GeneratorAccess generatoraccess, BlockPosition blockposition, RandomSource randomsource, int i) {
        generatoraccess.setBlock(blockposition, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition, Blocks.CHORUS_PLANT.defaultBlockState()), 2);
        growTreeRecursive(generatoraccess, blockposition, randomsource, blockposition, i, 0);
    }

    private static void growTreeRecursive(GeneratorAccess generatoraccess, BlockPosition blockposition, RandomSource randomsource, BlockPosition blockposition1, int i, int j) {
        Block block = Blocks.CHORUS_PLANT;
        int k = randomsource.nextInt(4) + 1;

        if (j == 0) {
            ++k;
        }

        for (int l = 0; l < k; ++l) {
            BlockPosition blockposition2 = blockposition.above(l + 1);

            if (!allNeighborsEmpty(generatoraccess, blockposition2, (EnumDirection) null)) {
                return;
            }

            generatoraccess.setBlock(blockposition2, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition2, block.defaultBlockState()), 2);
            generatoraccess.setBlock(blockposition2.below(), BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition2.below(), block.defaultBlockState()), 2);
        }

        boolean flag = false;

        if (j < 4) {
            int i1 = randomsource.nextInt(4);

            if (j == 0) {
                ++i1;
            }

            for (int j1 = 0; j1 < i1; ++j1) {
                EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
                BlockPosition blockposition3 = blockposition.above(k).relative(enumdirection);

                if (Math.abs(blockposition3.getX() - blockposition1.getX()) < i && Math.abs(blockposition3.getZ() - blockposition1.getZ()) < i && generatoraccess.isEmptyBlock(blockposition3) && generatoraccess.isEmptyBlock(blockposition3.below()) && allNeighborsEmpty(generatoraccess, blockposition3, enumdirection.getOpposite())) {
                    flag = true;
                    generatoraccess.setBlock(blockposition3, BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition3, block.defaultBlockState()), 2);
                    generatoraccess.setBlock(blockposition3.relative(enumdirection.getOpposite()), BlockChorusFruit.getStateWithConnections(generatoraccess, blockposition3.relative(enumdirection.getOpposite()), block.defaultBlockState()), 2);
                    growTreeRecursive(generatoraccess, blockposition3, randomsource, blockposition1, i, j + 1);
                }
            }
        }

        if (!flag) {
            generatoraccess.setBlock(blockposition.above(k), (IBlockData) Blocks.CHORUS_FLOWER.defaultBlockState().setValue(BlockChorusFlower.AGE, 5), 2);
        }

    }

    @Override
    protected void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

        if (!world.isClientSide && iprojectile.mayInteract(world, blockposition) && iprojectile.mayBreak(world)) {
            world.destroyBlock(blockposition, true, iprojectile);
        }

    }
}
