package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockWeepingVinesPlant extends BlockGrowingStem {

    public static final MapCodec<BlockWeepingVinesPlant> CODEC = simpleCodec(BlockWeepingVinesPlant::new);
    public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    @Override
    public MapCodec<BlockWeepingVinesPlant> codec() {
        return BlockWeepingVinesPlant.CODEC;
    }

    public BlockWeepingVinesPlant(BlockBase.Info blockbase_info) {
        super(blockbase_info, EnumDirection.DOWN, BlockWeepingVinesPlant.SHAPE, false);
    }

    @Override
    protected BlockGrowingTop getHeadBlock() {
        return (BlockGrowingTop) Blocks.WEEPING_VINES;
    }
}
