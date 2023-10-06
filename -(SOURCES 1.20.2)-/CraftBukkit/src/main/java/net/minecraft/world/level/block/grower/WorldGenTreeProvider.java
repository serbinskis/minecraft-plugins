package net.minecraft.world.level.block.grower;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;

// CraftBukkit start
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.BlockSapling;
import org.bukkit.TreeType;
// CraftBukkit end

public abstract class WorldGenTreeProvider {

    public WorldGenTreeProvider() {}

    @Nullable
    protected abstract ResourceKey<WorldGenFeatureConfigured<?, ?>> getConfiguredFeature(RandomSource randomsource, boolean flag);

    public boolean growTree(WorldServer worldserver, ChunkGenerator chunkgenerator, BlockPosition blockposition, IBlockData iblockdata, RandomSource randomsource) {
        ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey = this.getConfiguredFeature(randomsource, this.hasFlowers(worldserver, blockposition));

        if (resourcekey == null) {
            return false;
        } else {
            Holder<WorldGenFeatureConfigured<?, ?>> holder = (Holder) worldserver.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey).orElse(null); // CraftBukkit - decompile error

            if (holder == null) {
                return false;
            } else {
                setTreeType(holder); // CraftBukkit
                WorldGenFeatureConfigured<?, ?> worldgenfeatureconfigured = (WorldGenFeatureConfigured) holder.value();
                IBlockData iblockdata1 = worldserver.getFluidState(blockposition).createLegacyBlock();

                worldserver.setBlock(blockposition, iblockdata1, 4);
                if (worldgenfeatureconfigured.place(worldserver, chunkgenerator, randomsource, blockposition)) {
                    if (worldserver.getBlockState(blockposition) == iblockdata1) {
                        worldserver.sendBlockUpdated(blockposition, iblockdata, iblockdata1, 2);
                    }

                    return true;
                } else {
                    worldserver.setBlock(blockposition, iblockdata, 4);
                    return false;
                }
            }
        }
    }

    private boolean hasFlowers(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        Iterator iterator = BlockPosition.MutableBlockPosition.betweenClosed(blockposition.below().north(2).west(2), blockposition.above().south(2).east(2)).iterator();

        BlockPosition blockposition1;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            blockposition1 = (BlockPosition) iterator.next();
        } while (!generatoraccess.getBlockState(blockposition1).is(TagsBlock.FLOWERS));

        return true;
    }

    // CraftBukkit start
    protected void setTreeType(Holder<WorldGenFeatureConfigured<?, ?>> holder) {
        ResourceKey<WorldGenFeatureConfigured<?, ?>> worldgentreeabstract = holder.unwrapKey().get();
        if (worldgentreeabstract == TreeFeatures.OAK || worldgentreeabstract == TreeFeatures.OAK_BEES_005) {
            BlockSapling.treeType = TreeType.TREE;
        } else if (worldgentreeabstract == TreeFeatures.HUGE_RED_MUSHROOM) {
            BlockSapling.treeType = TreeType.RED_MUSHROOM;
        } else if (worldgentreeabstract == TreeFeatures.HUGE_BROWN_MUSHROOM) {
            BlockSapling.treeType = TreeType.BROWN_MUSHROOM;
        } else if (worldgentreeabstract == TreeFeatures.JUNGLE_TREE) {
            BlockSapling.treeType = TreeType.COCOA_TREE;
        } else if (worldgentreeabstract == TreeFeatures.JUNGLE_TREE_NO_VINE) {
            BlockSapling.treeType = TreeType.SMALL_JUNGLE;
        } else if (worldgentreeabstract == TreeFeatures.PINE) {
            BlockSapling.treeType = TreeType.TALL_REDWOOD;
        } else if (worldgentreeabstract == TreeFeatures.SPRUCE) {
            BlockSapling.treeType = TreeType.REDWOOD;
        } else if (worldgentreeabstract == TreeFeatures.ACACIA) {
            BlockSapling.treeType = TreeType.ACACIA;
        } else if (worldgentreeabstract == TreeFeatures.BIRCH || worldgentreeabstract == TreeFeatures.BIRCH_BEES_005) {
            BlockSapling.treeType = TreeType.BIRCH;
        } else if (worldgentreeabstract == TreeFeatures.SUPER_BIRCH_BEES_0002) {
            BlockSapling.treeType = TreeType.TALL_BIRCH;
        } else if (worldgentreeabstract == TreeFeatures.SWAMP_OAK) {
            BlockSapling.treeType = TreeType.SWAMP;
        } else if (worldgentreeabstract == TreeFeatures.FANCY_OAK || worldgentreeabstract == TreeFeatures.FANCY_OAK_BEES_005) {
            BlockSapling.treeType = TreeType.BIG_TREE;
        } else if (worldgentreeabstract == TreeFeatures.JUNGLE_BUSH) {
            BlockSapling.treeType = TreeType.JUNGLE_BUSH;
        } else if (worldgentreeabstract == TreeFeatures.DARK_OAK) {
            BlockSapling.treeType = TreeType.DARK_OAK;
        } else if (worldgentreeabstract == TreeFeatures.MEGA_SPRUCE) {
            BlockSapling.treeType = TreeType.MEGA_REDWOOD;
        } else if (worldgentreeabstract == TreeFeatures.MEGA_PINE) {
            BlockSapling.treeType = TreeType.MEGA_REDWOOD;
        } else if (worldgentreeabstract == TreeFeatures.MEGA_JUNGLE_TREE) {
            BlockSapling.treeType = TreeType.JUNGLE;
        } else if (worldgentreeabstract == TreeFeatures.AZALEA_TREE) {
            BlockSapling.treeType = TreeType.AZALEA;
        } else if (worldgentreeabstract == TreeFeatures.MANGROVE) {
            BlockSapling.treeType = TreeType.MANGROVE;
        } else if (worldgentreeabstract == TreeFeatures.TALL_MANGROVE) {
            BlockSapling.treeType = TreeType.TALL_MANGROVE;
        } else if (worldgentreeabstract == TreeFeatures.CHERRY || worldgentreeabstract == TreeFeatures.CHERRY_BEES_005) {
            BlockSapling.treeType = TreeType.CHERRY;
        } else {
            throw new IllegalArgumentException("Unknown tree generator " + worldgentreeabstract);
        }
    }
    // CraftBukkit end
}
