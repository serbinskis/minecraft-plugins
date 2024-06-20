package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolStructure;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public class PoolAliasBindings {

    public PoolAliasBindings() {}

    public static MapCodec<? extends PoolAliasBinding> bootstrap(IRegistry<MapCodec<? extends PoolAliasBinding>> iregistry) {
        IRegistry.register(iregistry, "random", Random.CODEC);
        IRegistry.register(iregistry, "random_group", RandomGroup.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "direct", Direct.CODEC);
    }

    public static void registerTargetsAsPools(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext, Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder, List<PoolAliasBinding> list) {
        list.stream().flatMap(PoolAliasBinding::allTargets).map((resourcekey) -> {
            return resourcekey.location().getPath();
        }).forEach((s) -> {
            WorldGenFeaturePieces.register(bootstrapcontext, s, new WorldGenFeatureDefinedStructurePoolTemplate(holder, List.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single(s), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        });
    }
}
