package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockGlazedTerracotta extends BlockFacingHorizontal {

    public static final MapCodec<BlockGlazedTerracotta> CODEC = simpleCodec(BlockGlazedTerracotta::new);

    @Override
    public MapCodec<BlockGlazedTerracotta> codec() {
        return BlockGlazedTerracotta.CODEC;
    }

    public BlockGlazedTerracotta(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockGlazedTerracotta.FACING);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockGlazedTerracotta.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }
}
