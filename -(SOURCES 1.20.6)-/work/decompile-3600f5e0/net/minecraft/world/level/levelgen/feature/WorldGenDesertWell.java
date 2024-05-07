package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;
import net.minecraft.world.level.storage.loot.LootTables;

public class WorldGenDesertWell extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final IBlockData sand;
    private final IBlockData sandSlab;
    private final IBlockData sandstone;
    private final IBlockData water;

    public WorldGenDesertWell(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
        this.sand = Blocks.SAND.defaultBlockState();
        this.sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
        this.sandstone = Blocks.SANDSTONE.defaultBlockState();
        this.water = Blocks.WATER.defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        BlockPosition blockposition = featureplacecontext.origin();

        for (blockposition = blockposition.above(); generatoraccessseed.isEmptyBlock(blockposition) && blockposition.getY() > generatoraccessseed.getMinBuildHeight() + 2; blockposition = blockposition.below()) {
            ;
        }

        if (!WorldGenDesertWell.IS_SAND.test(generatoraccessseed.getBlockState(blockposition))) {
            return false;
        } else {
            int i;
            int j;

            for (i = -2; i <= 2; ++i) {
                for (j = -2; j <= 2; ++j) {
                    if (generatoraccessseed.isEmptyBlock(blockposition.offset(i, -1, j)) && generatoraccessseed.isEmptyBlock(blockposition.offset(i, -2, j))) {
                        return false;
                    }
                }
            }

            int k;

            for (i = -2; i <= 0; ++i) {
                for (j = -2; j <= 2; ++j) {
                    for (k = -2; k <= 2; ++k) {
                        generatoraccessseed.setBlock(blockposition.offset(j, i, k), this.sandstone, 2);
                    }
                }
            }

            generatoraccessseed.setBlock(blockposition, this.water, 2);
            Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

            while (iterator.hasNext()) {
                EnumDirection enumdirection = (EnumDirection) iterator.next();

                generatoraccessseed.setBlock(blockposition.relative(enumdirection), this.water, 2);
            }

            BlockPosition blockposition1 = blockposition.below();

            generatoraccessseed.setBlock(blockposition1, this.sand, 2);
            Iterator iterator1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

            while (iterator1.hasNext()) {
                EnumDirection enumdirection1 = (EnumDirection) iterator1.next();

                generatoraccessseed.setBlock(blockposition1.relative(enumdirection1), this.sand, 2);
            }

            for (j = -2; j <= 2; ++j) {
                for (k = -2; k <= 2; ++k) {
                    if (j == -2 || j == 2 || k == -2 || k == 2) {
                        generatoraccessseed.setBlock(blockposition.offset(j, 1, k), this.sandstone, 2);
                    }
                }
            }

            generatoraccessseed.setBlock(blockposition.offset(2, 1, 0), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(-2, 1, 0), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(0, 1, 2), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(0, 1, -2), this.sandSlab, 2);

            for (j = -1; j <= 1; ++j) {
                for (k = -1; k <= 1; ++k) {
                    if (j == 0 && k == 0) {
                        generatoraccessseed.setBlock(blockposition.offset(j, 4, k), this.sandstone, 2);
                    } else {
                        generatoraccessseed.setBlock(blockposition.offset(j, 4, k), this.sandSlab, 2);
                    }
                }
            }

            for (j = 1; j <= 3; ++j) {
                generatoraccessseed.setBlock(blockposition.offset(-1, j, -1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(-1, j, 1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(1, j, -1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(1, j, 1), this.sandstone, 2);
            }

            List<BlockPosition> list = List.of(blockposition, blockposition.east(), blockposition.south(), blockposition.west(), blockposition.north());
            RandomSource randomsource = featureplacecontext.random();

            placeSusSand(generatoraccessseed, ((BlockPosition) SystemUtils.getRandom(list, randomsource)).below(1));
            placeSusSand(generatoraccessseed, ((BlockPosition) SystemUtils.getRandom(list, randomsource)).below(2));
            return true;
        }
    }

    private static void placeSusSand(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        generatoraccessseed.setBlock(blockposition, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        generatoraccessseed.getBlockEntity(blockposition, TileEntityTypes.BRUSHABLE_BLOCK).ifPresent((brushableblockentity) -> {
            brushableblockentity.setLootTable(LootTables.DESERT_WELL_ARCHAEOLOGY, blockposition.asLong());
        });
    }
}
