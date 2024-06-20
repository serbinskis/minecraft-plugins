package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;

public class MossBlock extends Block implements IBlockFragilePlantElement {

    public static final MapCodec<MossBlock> CODEC = simpleCodec(MossBlock::new);

    @Override
    public MapCodec<MossBlock> codec() {
        return MossBlock.CODEC;
    }

    public MossBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iworldreader.getBlockState(blockposition.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        worldserver.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((iregistry) -> {
            return iregistry.getHolder(CaveFeatures.MOSS_PATCH_BONEMEAL);
        }).ifPresent((holder_c) -> {
            ((WorldGenFeatureConfigured) holder_c.value()).place(worldserver, worldserver.getChunkSource().getGenerator(), randomsource, blockposition.above());
        });
    }

    @Override
    public IBlockFragilePlantElement.a getType() {
        return IBlockFragilePlantElement.a.NEIGHBOR_SPREADER;
    }
}
