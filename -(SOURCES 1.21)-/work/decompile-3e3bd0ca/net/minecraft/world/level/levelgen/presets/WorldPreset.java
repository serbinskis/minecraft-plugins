package net.minecraft.world.level.levelgen.presets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {

    public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), WorldDimension.CODEC).fieldOf("dimensions").forGetter((worldpreset) -> {
            return worldpreset.dimensions;
        })).apply(instance, WorldPreset::new);
    }).validate(WorldPreset::requireOverworld);
    public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC);
    private final Map<ResourceKey<WorldDimension>, WorldDimension> dimensions;

    public WorldPreset(Map<ResourceKey<WorldDimension>, WorldDimension> map) {
        this.dimensions = map;
    }

    private ImmutableMap<ResourceKey<WorldDimension>, WorldDimension> dimensionsInOrder() {
        Builder<ResourceKey<WorldDimension>, WorldDimension> builder = ImmutableMap.builder();

        WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach((resourcekey) -> {
            WorldDimension worlddimension = (WorldDimension) this.dimensions.get(resourcekey);

            if (worlddimension != null) {
                builder.put(resourcekey, worlddimension);
            }

        });
        return builder.build();
    }

    public WorldDimensions createWorldDimensions() {
        return new WorldDimensions(this.dimensionsInOrder());
    }

    public Optional<WorldDimension> overworld() {
        return Optional.ofNullable((WorldDimension) this.dimensions.get(WorldDimension.OVERWORLD));
    }

    private static DataResult<WorldPreset> requireOverworld(WorldPreset worldpreset) {
        return worldpreset.overworld().isEmpty() ? DataResult.error(() -> {
            return "Missing overworld dimension";
        }) : DataResult.success(worldpreset, Lifecycle.stable());
    }
}
