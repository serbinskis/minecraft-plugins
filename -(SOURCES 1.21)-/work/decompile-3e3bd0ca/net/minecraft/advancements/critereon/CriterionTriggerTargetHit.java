package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.phys.Vec3D;

public class CriterionTriggerTargetHit extends CriterionTriggerAbstract<CriterionTriggerTargetHit.a> {

    public CriterionTriggerTargetHit() {}

    @Override
    public Codec<CriterionTriggerTargetHit.a> codec() {
        return CriterionTriggerTargetHit.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, Vec3D vec3d, int i) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggertargethit_a) -> {
            return criteriontriggertargethit_a.matches(loottableinfo, vec3d, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, CriterionConditionValue.IntegerRange signalStrength, Optional<ContextAwarePredicate> projectile) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerTargetHit.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerTargetHit.a::player), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("signal_strength", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerTargetHit.a::signalStrength), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("projectile").forGetter(CriterionTriggerTargetHit.a::projectile)).apply(instance, CriterionTriggerTargetHit.a::new);
        });

        public static Criterion<CriterionTriggerTargetHit.a> targetHit(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, Optional<ContextAwarePredicate> optional) {
            return CriterionTriggers.TARGET_BLOCK_HIT.createCriterion(new CriterionTriggerTargetHit.a(Optional.empty(), criterionconditionvalue_integerrange, optional));
        }

        public boolean matches(LootTableInfo loottableinfo, Vec3D vec3d, int i) {
            return !this.signalStrength.matches(i) ? false : !this.projectile.isPresent() || ((ContextAwarePredicate) this.projectile.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.projectile, ".projectile");
        }
    }
}
