package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockHalfTransparent extends Block {

    public static final MapCodec<BlockHalfTransparent> CODEC = simpleCodec(BlockHalfTransparent::new);

    @Override
    protected MapCodec<? extends BlockHalfTransparent> codec() {
        return BlockHalfTransparent.CODEC;
    }

    protected BlockHalfTransparent(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected boolean skipRendering(IBlockData iblockdata, IBlockData iblockdata1, EnumDirection enumdirection) {
        return iblockdata1.is((Block) this) ? true : super.skipRendering(iblockdata, iblockdata1, enumdirection);
    }
}
