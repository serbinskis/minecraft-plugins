package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerSummonedEntity extends CriterionTriggerAbstract<CriterionTriggerSummonedEntity.a> {

    public CriterionTriggerSummonedEntity() {}

    @Override
    public Codec<CriterionTriggerSummonedEntity.a> codec() {
        return CriterionTriggerSummonedEntity.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggersummonedentity_a) -> {
            return criteriontriggersummonedentity_a.matches(loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerSummonedEntity.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerSummonedEntity.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerSummonedEntity.a::entity)).apply(instance, CriterionTriggerSummonedEntity.a::new);
        });

        public static Criterion<CriterionTriggerSummonedEntity.a> summonedEntity(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.SUMMONED_ENTITY.createCriterion(new CriterionTriggerSummonedEntity.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
        }

        public boolean matches(LootTableInfo loottableinfo) {
            return this.entity.isEmpty() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entity, ".entity");
        }
    }
}
