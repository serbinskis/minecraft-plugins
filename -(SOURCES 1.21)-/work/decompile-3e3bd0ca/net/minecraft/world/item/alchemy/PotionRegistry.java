package net.minecraft.world.item.alchemy;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class PotionRegistry implements FeatureElement {

    public static final Codec<Holder<PotionRegistry>> CODEC = BuiltInRegistries.POTION.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<PotionRegistry>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.POTION);
    @Nullable
    private final String name;
    private final List<MobEffect> effects;
    private FeatureFlagSet requiredFeatures;

    public PotionRegistry(MobEffect... amobeffect) {
        this((String) null, amobeffect);
    }

    public PotionRegistry(@Nullable String s, MobEffect... amobeffect) {
        this.requiredFeatures = FeatureFlags.VANILLA_SET;
        this.name = s;
        this.effects = List.of(amobeffect);
    }

    public PotionRegistry requiredFeatures(FeatureFlag... afeatureflag) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public static String getName(Optional<Holder<PotionRegistry>> optional, String s) {
        String s1;

        if (optional.isPresent()) {
            s1 = ((PotionRegistry) ((Holder) optional.get()).value()).name;
            if (s1 != null) {
                return s + s1;
            }
        }

        s1 = (String) optional.flatMap(Holder::unwrapKey).map((resourcekey) -> {
            return resourcekey.location().getPath();
        }).orElse("empty");
        return s + s1;
    }

    public List<MobEffect> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                if (((MobEffectList) mobeffect.getEffect().value()).isInstantenous()) {
                    return true;
                }
            }
        }

        return false;
    }
}
