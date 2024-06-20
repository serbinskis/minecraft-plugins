package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public class WorldGenFeaturePieces {

    public static final ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> EMPTY = createKey("empty");

    public WorldGenFeaturePieces() {}

    public static ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> createKey(String s) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MinecraftKey.withDefaultNamespace(s));
    }

    public static ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> parseKey(String s) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MinecraftKey.parse(s));
    }

    public static void register(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext, String s, WorldGenFeatureDefinedStructurePoolTemplate worldgenfeaturedefinedstructurepooltemplate) {
        bootstrapcontext.register(createKey(s), worldgenfeaturedefinedstructurepooltemplate);
    }

    public static void bootstrap(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext) {
        HolderGetter<WorldGenFeatureDefinedStructurePoolTemplate> holdergetter = bootstrapcontext.lookup(Registries.TEMPLATE_POOL);
        Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder = holdergetter.getOrThrow(WorldGenFeaturePieces.EMPTY);

        bootstrapcontext.register(WorldGenFeaturePieces.EMPTY, new WorldGenFeatureDefinedStructurePoolTemplate(holder, ImmutableList.of(), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeatureBastionPieces.bootstrap(bootstrapcontext);
        WorldGenFeaturePillagerOutpostPieces.bootstrap(bootstrapcontext);
        WorldGenFeatureVillages.bootstrap(bootstrapcontext);
        AncientCityStructurePieces.bootstrap(bootstrapcontext);
        TrailRuinsStructurePools.bootstrap(bootstrapcontext);
        TrialChambersStructurePools.bootstrap(bootstrapcontext);
    }
}
