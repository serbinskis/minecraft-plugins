package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class RootedDirtBlock extends Block implements IBlockFragilePlantElement {

    public static final MapCodec<RootedDirtBlock> CODEC = simpleCodec(RootedDirtBlock::new);

    @Override
    public MapCodec<RootedDirtBlock> codec() {
        return RootedDirtBlock.CODEC;
    }

    public RootedDirtBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iworldreader.getBlockState(blockposition.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockSpreadEvent(worldserver, blockposition, blockposition.below(), Blocks.HANGING_ROOTS.defaultBlockState()); // CraftBukkit
    }

    @Override
    public BlockPosition getParticlePos(BlockPosition blockposition) {
        return blockposition.below();
    }
}
