package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerTamedAnimal extends CriterionTriggerAbstract<CriterionTriggerTamedAnimal.a> {

    public CriterionTriggerTamedAnimal() {}

    @Override
    public Codec<CriterionTriggerTamedAnimal.a> codec() {
        return CriterionTriggerTamedAnimal.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, EntityAnimal entityanimal) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityanimal);

        this.trigger(entityplayer, (criteriontriggertamedanimal_a) -> {
            return criteriontriggertamedanimal_a.matches(loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerTamedAnimal.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerTamedAnimal.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerTamedAnimal.a::entity)).apply(instance, CriterionTriggerTamedAnimal.a::new);
        });

        public static Criterion<CriterionTriggerTamedAnimal.a> tamedAnimal() {
            return CriterionTriggers.TAME_ANIMAL.createCriterion(new CriterionTriggerTamedAnimal.a(Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerTamedAnimal.a> tamedAnimal(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.TAME_ANIMAL.createCriterion(new CriterionTriggerTamedAnimal.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
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
