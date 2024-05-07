package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.feature.WorldGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureRandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {

    public FeatureUtils() {}

    public static void bootstrap(BootstrapContext<WorldGenFeatureConfigured<?, ?>> bootstrapcontext) {
        AquaticFeatures.bootstrap(bootstrapcontext);
        CaveFeatures.bootstrap(bootstrapcontext);
        EndFeatures.bootstrap(bootstrapcontext);
        MiscOverworldFeatures.bootstrap(bootstrapcontext);
        NetherFeatures.bootstrap(bootstrapcontext);
        OreFeatures.bootstrap(bootstrapcontext);
        PileFeatures.bootstrap(bootstrapcontext);
        TreeFeatures.bootstrap(bootstrapcontext);
        VegetationFeatures.bootstrap(bootstrapcontext);
    }

    private static BlockPredicate simplePatchPredicate(List<Block> list) {
        BlockPredicate blockpredicate;

        if (!list.isEmpty()) {
            blockpredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(EnumDirection.DOWN.getNormal(), list));
        } else {
            blockpredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;
        }

        return blockpredicate;
    }

    public static WorldGenFeatureRandomPatchConfiguration simpleRandomPatchConfiguration(int i, Holder<PlacedFeature> holder) {
        return new WorldGenFeatureRandomPatchConfiguration(i, 7, 3, holder);
    }

    public static <FC extends WorldGenFeatureConfiguration, F extends WorldGenerator<FC>> WorldGenFeatureRandomPatchConfiguration simplePatchConfiguration(F f0, FC fc, List<Block> list, int i) {
        return simpleRandomPatchConfiguration(i, PlacementUtils.filtered(f0, fc, simplePatchPredicate(list)));
    }

    public static <FC extends WorldGenFeatureConfiguration, F extends WorldGenerator<FC>> WorldGenFeatureRandomPatchConfiguration simplePatchConfiguration(F f0, FC fc, List<Block> list) {
        return simplePatchConfiguration(f0, fc, list, 96);
    }

    public static <FC extends WorldGenFeatureConfiguration, F extends WorldGenerator<FC>> WorldGenFeatureRandomPatchConfiguration simplePatchConfiguration(F f0, FC fc) {
        return simplePatchConfiguration(f0, fc, List.of(), 96);
    }

    public static ResourceKey<WorldGenFeatureConfigured<?, ?>> createKey(String s) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new MinecraftKey(s));
    }

    public static void register(BootstrapContext<WorldGenFeatureConfigured<?, ?>> bootstrapcontext, ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey, WorldGenerator<WorldGenFeatureEmptyConfiguration> worldgenerator) {
        register(bootstrapcontext, resourcekey, worldgenerator, WorldGenFeatureConfiguration.NONE);
    }

    public static <FC extends WorldGenFeatureConfiguration, F extends WorldGenerator<FC>> void register(BootstrapContext<WorldGenFeatureConfigured<?, ?>> bootstrapcontext, ResourceKey<WorldGenFeatureConfigured<?, ?>> resourcekey, F f0, FC fc) {
        bootstrapcontext.register(resourcekey, new WorldGenFeatureConfigured<>(f0, fc));
    }
}
