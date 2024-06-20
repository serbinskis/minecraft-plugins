package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerEffectsChanged extends CriterionTriggerAbstract<CriterionTriggerEffectsChanged.a> {

    public CriterionTriggerEffectsChanged() {}

    @Override
    public Codec<CriterionTriggerEffectsChanged.a> codec() {
        return CriterionTriggerEffectsChanged.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, @Nullable Entity entity) {
        LootTableInfo loottableinfo = entity != null ? CriterionConditionEntity.createContext(entityplayer, entity) : null;

        this.trigger(entityplayer, (criteriontriggereffectschanged_a) -> {
            return criteriontriggereffectschanged_a.matches(entityplayer, loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionMobEffect> effects, Optional<ContextAwarePredicate> source) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerEffectsChanged.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerEffectsChanged.a::player), CriterionConditionMobEffect.CODEC.optionalFieldOf("effects").forGetter(CriterionTriggerEffectsChanged.a::effects), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("source").forGetter(CriterionTriggerEffectsChanged.a::source)).apply(instance, CriterionTriggerEffectsChanged.a::new);
        });

        public static Criterion<CriterionTriggerEffectsChanged.a> hasEffects(CriterionConditionMobEffect.a criterionconditionmobeffect_a) {
            return CriterionTriggers.EFFECTS_CHANGED.createCriterion(new CriterionTriggerEffectsChanged.a(Optional.empty(), criterionconditionmobeffect_a.build(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerEffectsChanged.a> gotEffectsFrom(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.EFFECTS_CHANGED.createCriterion(new CriterionTriggerEffectsChanged.a(Optional.empty(), Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a.build()))));
        }

        public boolean matches(EntityPlayer entityplayer, @Nullable LootTableInfo loottableinfo) {
            return this.effects.isPresent() && !((CriterionConditionMobEffect) this.effects.get()).matches((EntityLiving) entityplayer) ? false : !this.source.isPresent() || loottableinfo != null && ((ContextAwarePredicate) this.source.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.source, ".source");
        }
    }
}
