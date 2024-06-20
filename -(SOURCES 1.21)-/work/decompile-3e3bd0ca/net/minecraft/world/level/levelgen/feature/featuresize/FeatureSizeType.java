package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FeatureSizeType<P extends FeatureSize> {

    public static final FeatureSizeType<FeatureSizeTwoLayers> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", FeatureSizeTwoLayers.CODEC);
    public static final FeatureSizeType<FeatureSizeThreeLayers> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", FeatureSizeThreeLayers.CODEC);
    private final MapCodec<P> codec;

    private static <P extends FeatureSize> FeatureSizeType<P> register(String s, MapCodec<P> mapcodec) {
        return (FeatureSizeType) IRegistry.register(BuiltInRegistries.FEATURE_SIZE_TYPE, s, new FeatureSizeType<>(mapcodec));
    }

    private FeatureSizeType(MapCodec<P> mapcodec) {
        this.codec = mapcodec;
    }

    public MapCodec<P> codec() {
        return this.codec;
    }
}
