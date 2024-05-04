package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class TintedGlassBlock extends BlockGlassAbstract {

    public static final MapCodec<TintedGlassBlock> CODEC = simpleCodec(TintedGlassBlock::new);

    @Override
    public MapCodec<TintedGlassBlock> codec() {
        return TintedGlassBlock.CODEC;
    }

    public TintedGlassBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return false;
    }

    @Override
    protected int getLightBlock(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockaccess.getMaxLightLevel();
    }
}
