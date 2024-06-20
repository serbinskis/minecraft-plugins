package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

@FunctionalInterface
public interface PoolAliasLookup {

    PoolAliasLookup EMPTY = (resourcekey) -> {
        return resourcekey;
    };

    ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> lookup(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey);

    static PoolAliasLookup create(List<PoolAliasBinding> list, BlockPosition blockposition, long i) {
        if (list.isEmpty()) {
            return PoolAliasLookup.EMPTY;
        } else {
            RandomSource randomsource = RandomSource.create(i).forkPositional().at(blockposition);
            Builder<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> builder = ImmutableMap.builder();

            list.forEach((poolaliasbinding) -> {
                Objects.requireNonNull(builder);
                poolaliasbinding.forEachResolved(randomsource, builder::put);
            });
            Map<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> map = builder.build();

            return (resourcekey) -> {
                return (ResourceKey) Objects.requireNonNull((ResourceKey) map.getOrDefault(resourcekey, resourcekey), () -> {
                    return "alias " + String.valueOf(resourcekey) + " was mapped to null value";
                });
            };
        }
    }
}
