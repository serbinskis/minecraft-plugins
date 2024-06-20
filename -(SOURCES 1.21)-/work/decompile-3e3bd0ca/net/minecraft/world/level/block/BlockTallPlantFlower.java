package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockTallPlantFlower extends BlockTallPlant implements IBlockFragilePlantElement {

    public static final MapCodec<BlockTallPlantFlower> CODEC = simpleCodec(BlockTallPlantFlower::new);

    @Override
    public MapCodec<BlockTallPlantFlower> codec() {
        return BlockTallPlantFlower.CODEC;
    }

    public BlockTallPlantFlower(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        popResource(worldserver, blockposition, new ItemStack(this));
    }
}
