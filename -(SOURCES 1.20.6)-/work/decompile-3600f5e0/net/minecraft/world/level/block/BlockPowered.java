package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockPowered extends Block {

    public static final MapCodec<BlockPowered> CODEC = simpleCodec(BlockPowered::new);

    @Override
    public MapCodec<BlockPowered> codec() {
        return BlockPowered.CODEC;
    }

    public BlockPowered(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return 15;
    }
}
