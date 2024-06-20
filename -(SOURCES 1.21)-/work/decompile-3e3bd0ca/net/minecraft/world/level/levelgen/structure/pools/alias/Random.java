package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

record Random(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> alias, SimpleWeightedRandomList<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> targets) implements PoolAliasBinding {

    static MapCodec<Random> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(Random::alias), SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(Registries.TEMPLATE_POOL)).fieldOf("targets").forGetter(Random::targets)).apply(instance, Random::new);
    });

    @Override
    public void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer) {
        this.targets.getRandom(randomsource).ifPresent((weightedentry_b) -> {
            biconsumer.accept(this.alias, (ResourceKey) weightedentry_b.data());
        });
    }

    @Override
    public Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets() {
        return this.targets.unwrap().stream().map(WeightedEntry.b::data);
    }

    @Override
    public MapCodec<Random> codec() {
        return Random.CODEC;
    }
}
