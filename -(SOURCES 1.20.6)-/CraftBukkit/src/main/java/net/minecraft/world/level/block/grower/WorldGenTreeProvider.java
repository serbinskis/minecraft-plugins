package net.minecraft.world.level.block.grower;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;

// CraftBukkit start
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.BlockSapling;
import org.bukkit.TreeType;
// CraftBukkit end

public final class WorldGenTreeProvider {

    private static final Map<String, WorldGenTreeProvider> GROWERS = new Object2ObjectArrayMap();
    public static final Codec<WorldGenTreeProvider> CODEC;
    public static final WorldGenTreeProvider OAK;
    public static final WorldGenTreeProvider SPRUCE;
    public static final WorldGenTreeProvider MANGROVE;
    public static final WorldGenTreeProvider AZALEA;
    public static final WorldGenTreeProvider BIRCH;
    public static final WorldGenTreeProvider JUNGLE;
    public static final WorldGenTreeProvider ACACIA;
    public static final WorldGenTreeProvider CHERRY;
    public static final WorldGenTreeProvider DARK_OAK;
    private final String name;
    private final float secondaryChance;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> megaTree;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> secondaryMegaTree;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> tree;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> secondaryTree;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> flowers;
    private final Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> secondaryFlowers;

    public WorldGenTreeProvider(String s, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional1, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional2) {
        this(s, 0.0F, optional, Optional.empty(), optional1, Optional.empty(), optional2, Optional.empty());
    }

    public WorldGenTreeProvider(String s, float f, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional1, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional2, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional3, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional4, Optional<ResourceKey<WorldGenFeatureConfigured<?, ?>>> optional5) {
        this.name = s;
        this.secondaryChance = f;
        this.megaTree = optional;
        this.secondaryMegaTree = optional1;
        this.tree = optional2;
        this.secondaryTree = optional3;
        this.flowers = optional4;
        this.secondaryFlowers = optional5;
        WorldGenTreeProvider.GROWERS.put(s, this);
    }

    @Nullable
    private ResourceKey<WorldGenFeatureConfigured<?, ?>> getConfiguredFeature(RandomSource randomsource, boolean flag) {
        if (randomsource.nextFloat() < this.secondaryChance) {
            if (flag && this.secondaryFlowers.isPresent()) {
                return (ResourceKey) this.secondaryFlowers.get();
            }

            if (this.secondaryTree.isPresent()) {
                return (ResourceKey) this.secondaryTree.get();
            }
        }

        return flag && this.flowers.isPresent() ? (ResourceKey) this.flowers.get() : (ResourceKey) this.tree.orElse(null); // CraftBukkit - decompile error
    }

    @Nullable
    private ResourceKey<WorldGenFeatureConfigured<?, ?>> getConfiguredMegaFeature(RandomSource randomsource) {
        return this.secondaryMegaTree.isPresent() && randomsource.nextFloat() < this.secondaryChance ? (ResourceKey) this.secondaryMegaTree.get() : (ResourceKey) this.megaTree.orElse(null); // CraftBukkit - decompile error
    }

    public boolean growTree(WorldServer worldserver, ChunkGenerator chunkgenerator, BlockPosition blockposition, IBlockData iblockdata, RandomSource randomsource) {
        ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey = this.getConfiguredMegaFeature(randomsource);

        if (resourcekey != null) {
            Holder<WorldGenFeatureConfigured<?, ?>> holder = (Holder) worldserver.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey).orElse(null); // CraftBukkit - decompile error

            if (holder != null) {
                setTreeType(holder); // CraftBukkit
                for (int i = 0; i >= -1; --i) {
                    for (int j = 0; j >= -1; --j) {
                        if (isTwoByTwoSapling(iblockdata, worldserver, blockposition, i, j)) {
                            WorldGenFeatureConfigured<?, ?> worldgenfeatureconfigured = (WorldGenFeatureConfigured) holder.value();
                            IBlockData iblockdata1 = Blocks.AIR.defaultBlockState();

                            worldserver.setBlock(blockposition.offset(i, 0, j), iblockdata1, 4);
                            worldserver.setBlock(blockposition.offset(i + 1, 0, j), iblockdata1, 4);
                            worldserver.setBlock(blockposition.offset(i, 0, j + 1), iblockdata1, 4);
                            worldserver.setBlock(blockposition.offset(i + 1, 0, j + 1), iblockdata1, 4);
                            if (worldgenfeatureconfigured.place(worldserver, chunkgenerator, randomsource, blockposition.offset(i, 0, j))) {
                                return true;
                            }

                            worldserver.setBlock(blockposition.offset(i, 0, j), iblockdata, 4);
                            worldserver.setBlock(blockposition.offset(i + 1, 0, j), iblockdata, 4);
                            worldserver.setBlock(blockposition.offset(i, 0, j + 1), iblockdata, 4);
                            worldserver.setBlock(blockposition.offset(i + 1, 0, j + 1), iblockdata, 4);
                            return false;
                        }
                    }
                }
            }
        }

        ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey1 = this.getConfiguredFeature(randomsource, this.hasFlowers(worldserver, blockposition));

        if (resourcekey1 == null) {
            return false;
        } else {
            Holder<WorldGenFeatureConfigured<?, ?>> holder1 = (Holder) worldserver.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey1).orElse(null); // CraftBukkit - decompile error

            if (holder1 == null) {
                return false;
            } else {
                setTreeType(holder1); // CraftBukkit
                WorldGenFeatureConfigured<?, ?> worldgenfeatureconfigured1 = (WorldGenFeatureConfigured) holder1.value();
                IBlockData iblockdata2 = worldserver.getFluidState(blockposition).createLegacyBlock();

                worldserver.setBlock(blockposition, iblockdata2, 4);
                if (worldgenfeatureconfigured1.place(worldserver, chunkgenerator, randomsource, blockposition)) {
                    if (worldserver.getBlockState(blockposition) == iblockdata2) {
                        worldserver.sendBlockUpdated(blockposition, iblockdata, iblockdata2, 2);
                    }

                    return true;
                } else {
                    worldserver.setBlock(blockposition, iblockdata, 4);
                    return false;
                }
            }
        }
    }

    private static boolean isTwoByTwoSapling(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, int i, int j) {
        Block block = iblockdata.getBlock();

        return iblockaccess.getBlockState(blockposition.offset(i, 0, j)).is(block) && iblockaccess.getBlockState(blockposition.offset(i + 1, 0, j)).is(block) && iblockaccess.getBlockState(blockposition.offset(i, 0, j + 1)).is(block) && iblockaccess.getBlockState(blockposition.offset(i + 1, 0, j + 1)).is(block);
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
    private void setTreeType(Holder<WorldGenFeatureConfigured<?, ?>> holder) {
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

    static {
        Function<WorldGenTreeProvider, String> function = (worldgentreeprovider) -> { // CraftBukkit - decompile error
            return worldgentreeprovider.name;
        };
        Map<String, WorldGenTreeProvider> map = WorldGenTreeProvider.GROWERS; // CraftBukkit - decompile error

        Objects.requireNonNull(map);
        CODEC = Codec.stringResolver(function, map::get);
        OAK = new WorldGenTreeProvider("oak", 0.1F, Optional.empty(), Optional.empty(), Optional.of(TreeFeatures.OAK), Optional.of(TreeFeatures.FANCY_OAK), Optional.of(TreeFeatures.OAK_BEES_005), Optional.of(TreeFeatures.FANCY_OAK_BEES_005));
        SPRUCE = new WorldGenTreeProvider("spruce", 0.5F, Optional.of(TreeFeatures.MEGA_SPRUCE), Optional.of(TreeFeatures.MEGA_PINE), Optional.of(TreeFeatures.SPRUCE), Optional.empty(), Optional.empty(), Optional.empty());
        MANGROVE = new WorldGenTreeProvider("mangrove", 0.85F, Optional.empty(), Optional.empty(), Optional.of(TreeFeatures.MANGROVE), Optional.of(TreeFeatures.TALL_MANGROVE), Optional.empty(), Optional.empty());
        AZALEA = new WorldGenTreeProvider("azalea", Optional.empty(), Optional.of(TreeFeatures.AZALEA_TREE), Optional.empty());
        BIRCH = new WorldGenTreeProvider("birch", Optional.empty(), Optional.of(TreeFeatures.BIRCH), Optional.of(TreeFeatures.BIRCH_BEES_005));
        JUNGLE = new WorldGenTreeProvider("jungle", Optional.of(TreeFeatures.MEGA_JUNGLE_TREE), Optional.of(TreeFeatures.JUNGLE_TREE_NO_VINE), Optional.empty());
        ACACIA = new WorldGenTreeProvider("acacia", Optional.empty(), Optional.of(TreeFeatures.ACACIA), Optional.empty());
        CHERRY = new WorldGenTreeProvider("cherry", Optional.empty(), Optional.of(TreeFeatures.CHERRY), Optional.of(TreeFeatures.CHERRY_BEES_005));
        DARK_OAK = new WorldGenTreeProvider("dark_oak", Optional.of(TreeFeatures.DARK_OAK), Optional.empty(), Optional.empty());
    }
}
