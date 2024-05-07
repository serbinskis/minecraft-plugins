package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;

public record SuspiciousStewEffects(List<SuspiciousStewEffects.a> effects) {

    public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
    public static final Codec<SuspiciousStewEffects> CODEC = SuspiciousStewEffects.a.CODEC.listOf().xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
    public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = SuspiciousStewEffects.a.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

    public SuspiciousStewEffects withEffectAdded(SuspiciousStewEffects.a suspicioussteweffects_a) {
        return new SuspiciousStewEffects(SystemUtils.copyAndAdd(this.effects, (Object) suspicioussteweffects_a));
    }

    public static record a(Holder<MobEffectList> effect, int duration) {

        public static final Codec<SuspiciousStewEffects.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("id").forGetter(SuspiciousStewEffects.a::effect), Codec.INT.lenientOptionalFieldOf("duration", 160).forGetter(SuspiciousStewEffects.a::duration)).apply(instance, SuspiciousStewEffects.a::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), SuspiciousStewEffects.a::effect, ByteBufCodecs.VAR_INT, SuspiciousStewEffects.a::duration, SuspiciousStewEffects.a::new);

        public MobEffect createEffectInstance() {
            return new MobEffect(this.effect, this.duration);
        }
    }
}
