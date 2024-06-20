package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class LavaCauldronBlock extends AbstractCauldronBlock {

    public static final MapCodec<LavaCauldronBlock> CODEC = simpleCodec(LavaCauldronBlock::new);

    @Override
    public MapCodec<LavaCauldronBlock> codec() {
        return LavaCauldronBlock.CODEC;
    }

    public LavaCauldronBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info, CauldronInteraction.LAVA);
    }

    @Override
    protected double getContentHeight(IBlockData iblockdata) {
        return 0.9375D;
    }

    @Override
    public boolean isFull(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (this.isEntityInsideContent(iblockdata, blockposition, entity)) {
            entity.lavaHurt();
        }

    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return 3;
    }
}
