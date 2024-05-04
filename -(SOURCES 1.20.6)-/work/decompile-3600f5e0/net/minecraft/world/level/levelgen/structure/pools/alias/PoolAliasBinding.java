package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public interface PoolAliasBinding {

    Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

    void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer);

    Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets();

    static Direct direct(String s, String s1) {
        return direct(WorldGenFeaturePieces.createKey(s), WorldGenFeaturePieces.createKey(s1));
    }

    static Direct direct(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey1) {
        return new Direct(resourcekey, resourcekey1);
    }

    static Random random(String s, SimpleWeightedRandomList<String> simpleweightedrandomlist) {
        SimpleWeightedRandomList.a<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> simpleweightedrandomlist_a = SimpleWeightedRandomList.builder();

        simpleweightedrandomlist.unwrap().forEach((weightedentry_b) -> {
            simpleweightedrandomlist_a.add(WorldGenFeaturePieces.createKey((String) weightedentry_b.data()), weightedentry_b.getWeight().asInt());
        });
        return random(WorldGenFeaturePieces.createKey(s), simpleweightedrandomlist_a.build());
    }

    static Random random(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey, SimpleWeightedRandomList<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> simpleweightedrandomlist) {
        return new Random(resourcekey, simpleweightedrandomlist);
    }

    static RandomGroup randomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> simpleweightedrandomlist) {
        return new RandomGroup(simpleweightedrandomlist);
    }

    MapCodec<? extends PoolAliasBinding> codec();
}
