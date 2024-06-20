package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;

public record CriterionConditionMobEffect(Map<Holder<MobEffectList>, CriterionConditionMobEffect.b> effectMap) {

    public static final Codec<CriterionConditionMobEffect> CODEC = Codec.unboundedMap(MobEffectList.CODEC, CriterionConditionMobEffect.b.CODEC).xmap(CriterionConditionMobEffect::new, CriterionConditionMobEffect::effectMap);

    public boolean matches(Entity entity) {
        boolean flag;

        if (entity instanceof EntityLiving entityliving) {
            if (this.matches(entityliving.getActiveEffectsMap())) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    public boolean matches(EntityLiving entityliving) {
        return this.matches(entityliving.getActiveEffectsMap());
    }

    public boolean matches(Map<Holder<MobEffectList>, MobEffect> map) {
        Iterator iterator = this.effectMap.entrySet().iterator();

        Entry entry;
        MobEffect mobeffect;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            entry = (Entry) iterator.next();
            mobeffect = (MobEffect) map.get(entry.getKey());
        } while (((CriterionConditionMobEffect.b) entry.getValue()).matches(mobeffect));

        return false;
    }

    public static record b(CriterionConditionValue.IntegerRange amplifier, CriterionConditionValue.IntegerRange duration, Optional<Boolean> ambient, Optional<Boolean> visible) {

        public static final Codec<CriterionConditionMobEffect.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("amplifier", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionMobEffect.b::amplifier), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("duration", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionMobEffect.b::duration), Codec.BOOL.optionalFieldOf("ambient").forGetter(CriterionConditionMobEffect.b::ambient), Codec.BOOL.optionalFieldOf("visible").forGetter(CriterionConditionMobEffect.b::visible)).apply(instance, CriterionConditionMobEffect.b::new);
        });

        public b() {
            this(CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY, Optional.empty(), Optional.empty());
        }

        public boolean matches(@Nullable MobEffect mobeffect) {
            return mobeffect == null ? false : (!this.amplifier.matches(mobeffect.getAmplifier()) ? false : (!this.duration.matches(mobeffect.getDuration()) ? false : (this.ambient.isPresent() && (Boolean) this.ambient.get() != mobeffect.isAmbient() ? false : !this.visible.isPresent() || (Boolean) this.visible.get() == mobeffect.isVisible())));
        }
    }

    public static class a {

        private final Builder<Holder<MobEffectList>, CriterionConditionMobEffect.b> effectMap = ImmutableMap.builder();

        public a() {}

        public static CriterionConditionMobEffect.a effects() {
            return new CriterionConditionMobEffect.a();
        }

        public CriterionConditionMobEffect.a and(Holder<MobEffectList> holder) {
            this.effectMap.put(holder, new CriterionConditionMobEffect.b());
            return this;
        }

        public CriterionConditionMobEffect.a and(Holder<MobEffectList> holder, CriterionConditionMobEffect.b criterionconditionmobeffect_b) {
            this.effectMap.put(holder, criterionconditionmobeffect_b);
            return this;
        }

        public Optional<CriterionConditionMobEffect> build() {
            return Optional.of(new CriterionConditionMobEffect(this.effectMap.build()));
        }
    }
}
