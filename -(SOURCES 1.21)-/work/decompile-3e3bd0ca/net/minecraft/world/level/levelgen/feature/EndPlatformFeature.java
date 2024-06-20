package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;

public class EndPlatformFeature extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    public EndPlatformFeature(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        createEndPlatform(featureplacecontext.level(), featureplacecontext.origin(), false);
        return true;
    }

    public static void createEndPlatform(WorldAccess worldaccess, BlockPosition blockposition, boolean flag) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = blockposition_mutableblockposition.set(blockposition).move(j, k, i);
                    Block block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;

                    if (!worldaccess.getBlockState(blockposition_mutableblockposition1).is(block)) {
                        if (flag) {
                            worldaccess.destroyBlock(blockposition_mutableblockposition1, true, (Entity) null);
                        }

                        worldaccess.setBlock(blockposition_mutableblockposition1, block.defaultBlockState(), 3);
                    }
                }
            }
        }

    }
}
