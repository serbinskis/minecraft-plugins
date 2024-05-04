package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EnumColor;
import org.slf4j.Logger;

public record BannerPatternLayers(List<BannerPatternLayers.b> layers) {

    static final Logger LOGGER = LogUtils.getLogger();
    public static final BannerPatternLayers EMPTY = new BannerPatternLayers(List.of());
    public static final Codec<BannerPatternLayers> CODEC = BannerPatternLayers.b.CODEC.listOf().xmap(BannerPatternLayers::new, BannerPatternLayers::layers);
    public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers> STREAM_CODEC = BannerPatternLayers.b.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BannerPatternLayers::new, BannerPatternLayers::layers);

    public BannerPatternLayers removeLast() {
        return new BannerPatternLayers(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
    }

    public static record b(Holder<EnumBannerPatternType> pattern, EnumColor color) {

        public static final Codec<BannerPatternLayers.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(EnumBannerPatternType.CODEC.fieldOf("pattern").forGetter(BannerPatternLayers.b::pattern), EnumColor.CODEC.fieldOf("color").forGetter(BannerPatternLayers.b::color)).apply(instance, BannerPatternLayers.b::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers.b> STREAM_CODEC = StreamCodec.composite(EnumBannerPatternType.STREAM_CODEC, BannerPatternLayers.b::pattern, EnumColor.STREAM_CODEC, BannerPatternLayers.b::color, BannerPatternLayers.b::new);

        public IChatMutableComponent description() {
            String s = ((EnumBannerPatternType) this.pattern.value()).translationKey();

            return IChatBaseComponent.translatable(s + "." + this.color.getName());
        }
    }

    public static class a {

        private final Builder<BannerPatternLayers.b> layers = ImmutableList.builder();

        public a() {}

        /** @deprecated */
        @Deprecated
        public BannerPatternLayers.a addIfRegistered(HolderGetter<EnumBannerPatternType> holdergetter, ResourceKey<EnumBannerPatternType> resourcekey, EnumColor enumcolor) {
            Optional<Holder.c<EnumBannerPatternType>> optional = holdergetter.get(resourcekey);

            if (optional.isEmpty()) {
                BannerPatternLayers.LOGGER.warn("Unable to find banner pattern with id: '{}'", resourcekey.location());
                return this;
            } else {
                return this.add((Holder) optional.get(), enumcolor);
            }
        }

        public BannerPatternLayers.a add(Holder<EnumBannerPatternType> holder, EnumColor enumcolor) {
            return this.add(new BannerPatternLayers.b(holder, enumcolor));
        }

        public BannerPatternLayers.a add(BannerPatternLayers.b bannerpatternlayers_b) {
            this.layers.add(bannerpatternlayers_b);
            return this;
        }

        public BannerPatternLayers.a addAll(BannerPatternLayers bannerpatternlayers) {
            this.layers.addAll(bannerpatternlayers.layers);
            return this;
        }

        public BannerPatternLayers build() {
            return new BannerPatternLayers(this.layers.build());
        }
    }
}
