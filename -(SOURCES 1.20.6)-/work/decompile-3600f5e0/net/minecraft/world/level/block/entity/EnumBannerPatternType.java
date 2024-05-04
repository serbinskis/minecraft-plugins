package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;

public record EnumBannerPatternType(MinecraftKey assetId, String translationKey) {

    public static final Codec<EnumBannerPatternType> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("asset_id").forGetter(EnumBannerPatternType::assetId), Codec.STRING.fieldOf("translation_key").forGetter(EnumBannerPatternType::translationKey)).apply(instance, EnumBannerPatternType::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, EnumBannerPatternType> DIRECT_STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, EnumBannerPatternType::assetId, ByteBufCodecs.STRING_UTF8, EnumBannerPatternType::translationKey, EnumBannerPatternType::new);
    public static final Codec<Holder<EnumBannerPatternType>> CODEC = RegistryFileCodec.create(Registries.BANNER_PATTERN, EnumBannerPatternType.DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<EnumBannerPatternType>> STREAM_CODEC = ByteBufCodecs.holder(Registries.BANNER_PATTERN, EnumBannerPatternType.DIRECT_STREAM_CODEC);
}
