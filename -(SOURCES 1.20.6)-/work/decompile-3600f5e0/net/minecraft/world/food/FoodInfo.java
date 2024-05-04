package net.minecraft.world.food;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;

public record FoodInfo(int nutrition, float saturation, boolean canAlwaysEat, float eatSeconds, List<FoodInfo.b> effects) {

    private static final float DEFAULT_EAT_SECONDS = 1.6F;
    public static final Codec<FoodInfo> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodInfo::nutrition), Codec.FLOAT.fieldOf("saturation").forGetter(FoodInfo::saturation), Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodInfo::canAlwaysEat), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("eat_seconds", 1.6F).forGetter(FoodInfo::eatSeconds), FoodInfo.b.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodInfo::effects)).apply(instance, FoodInfo::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodInfo> DIRECT_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FoodInfo::nutrition, ByteBufCodecs.FLOAT, FoodInfo::saturation, ByteBufCodecs.BOOL, FoodInfo::canAlwaysEat, ByteBufCodecs.FLOAT, FoodInfo::eatSeconds, FoodInfo.b.STREAM_CODEC.apply(ByteBufCodecs.list()), FoodInfo::effects, FoodInfo::new);

    public int eatDurationTicks() {
        return (int) (this.eatSeconds * 20.0F);
    }

    public static record b(MobEffect effect, float probability) {

        public static final Codec<FoodInfo.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MobEffect.CODEC.fieldOf("effect").forGetter(FoodInfo.b::effect), Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(FoodInfo.b::probability)).apply(instance, FoodInfo.b::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, FoodInfo.b> STREAM_CODEC = StreamCodec.composite(MobEffect.STREAM_CODEC, FoodInfo.b::effect, ByteBufCodecs.FLOAT, FoodInfo.b::probability, FoodInfo.b::new);

        public MobEffect effect() {
            return new MobEffect(this.effect);
        }
    }

    public static class a {

        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;
        private float eatSeconds = 1.6F;
        private final Builder<FoodInfo.b> effects = ImmutableList.builder();

        public a() {}

        public FoodInfo.a nutrition(int i) {
            this.nutrition = i;
            return this;
        }

        public FoodInfo.a saturationModifier(float f) {
            this.saturationModifier = f;
            return this;
        }

        public FoodInfo.a alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public FoodInfo.a fast() {
            this.eatSeconds = 0.8F;
            return this;
        }

        public FoodInfo.a effect(MobEffect mobeffect, float f) {
            this.effects.add(new FoodInfo.b(mobeffect, f));
            return this;
        }

        public FoodInfo build() {
            float f = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);

            return new FoodInfo(this.nutrition, f, this.canAlwaysEat, this.eatSeconds, this.effects.build());
        }
    }
}
