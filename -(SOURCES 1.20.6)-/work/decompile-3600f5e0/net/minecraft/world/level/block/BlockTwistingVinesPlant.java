package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTwistingVinesPlant extends BlockGrowingStem {

    public static final MapCodec<BlockTwistingVinesPlant> CODEC = simpleCodec(BlockTwistingVinesPlant::new);
    public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    @Override
    public MapCodec<BlockTwistingVinesPlant> codec() {
        return BlockTwistingVinesPlant.CODEC;
    }

    public BlockTwistingVinesPlant(BlockBase.Info blockbase_info) {
        super(blockbase_info, EnumDirection.UP, BlockTwistingVinesPlant.SHAPE, false);
    }

    @Override
    protected BlockGrowingTop getHeadBlock() {
        return (BlockGrowingTop) Blocks.TWISTING_VINES;
    }
}
