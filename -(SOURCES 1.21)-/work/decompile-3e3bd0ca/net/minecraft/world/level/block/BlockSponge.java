package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class BlockSponge extends Block {

    public static final MapCodec<BlockSponge> CODEC = simpleCodec(BlockSponge::new);
    public static final int MAX_DEPTH = 6;
    public static final int MAX_COUNT = 64;
    private static final EnumDirection[] ALL_DIRECTIONS = EnumDirection.values();

    @Override
    public MapCodec<BlockSponge> codec() {
        return BlockSponge.CODEC;
    }

    protected BlockSponge(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            this.tryAbsorbWater(world, blockposition);
        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        this.tryAbsorbWater(world, blockposition);
        super.neighborChanged(iblockdata, world, blockposition, block, blockposition1, flag);
    }

    protected void tryAbsorbWater(World world, BlockPosition blockposition) {
        if (this.removeWaterBreadthFirstSearch(world, blockposition)) {
            world.setBlock(blockposition, Blocks.WET_SPONGE.defaultBlockState(), 2);
            world.playSound((EntityHuman) null, blockposition, SoundEffects.SPONGE_ABSORB, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

    }

    private boolean removeWaterBreadthFirstSearch(World world, BlockPosition blockposition) {
        return BlockPosition.breadthFirstTraversal(blockposition, 6, 65, (blockposition1, consumer) -> {
            EnumDirection[] aenumdirection = BlockSponge.ALL_DIRECTIONS;
            int i = aenumdirection.length;

            for (int j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];

                consumer.accept(blockposition1.relative(enumdirection));
            }

        }, (blockposition1) -> {
            if (blockposition1.equals(blockposition)) {
                return true;
            } else {
                IBlockData iblockdata = world.getBlockState(blockposition1);
                Fluid fluid = world.getFluidState(blockposition1);

                if (!fluid.is(TagsFluid.WATER)) {
                    return false;
                } else {
                    Block block = iblockdata.getBlock();

                    if (block instanceof IFluidSource) {
                        IFluidSource ifluidsource = (IFluidSource) block;

                        if (!ifluidsource.pickupBlock((EntityHuman) null, world, blockposition1, iblockdata).isEmpty()) {
                            return true;
                        }
                    }

                    if (iblockdata.getBlock() instanceof BlockFluids) {
                        world.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (!iblockdata.is(Blocks.KELP) && !iblockdata.is(Blocks.KELP_PLANT) && !iblockdata.is(Blocks.SEAGRASS) && !iblockdata.is(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        TileEntity tileentity = iblockdata.hasBlockEntity() ? world.getBlockEntity(blockposition1) : null;

                        dropResources(iblockdata, world, blockposition1, tileentity);
                        world.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return true;
                }
            }
        }) > 1;
    }
}
