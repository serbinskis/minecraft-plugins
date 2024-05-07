package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockWeepingVines extends BlockGrowingTop {

    public static final MapCodec<BlockWeepingVines> CODEC = simpleCodec(BlockWeepingVines::new);
    protected static final VoxelShape SHAPE = Block.box(4.0D, 9.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    @Override
    public MapCodec<BlockWeepingVines> codec() {
        return BlockWeepingVines.CODEC;
    }

    public BlockWeepingVines(BlockBase.Info blockbase_info) {
        super(blockbase_info, EnumDirection.DOWN, BlockWeepingVines.SHAPE, false, 0.1D);
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource randomsource) {
        return BlockNetherVinesUtil.getBlocksToGrowWhenBonemealed(randomsource);
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.WEEPING_VINES_PLANT;
    }

    @Override
    protected boolean canGrowInto(IBlockData iblockdata) {
        return BlockNetherVinesUtil.isValidGrowthState(iblockdata);
    }
}
