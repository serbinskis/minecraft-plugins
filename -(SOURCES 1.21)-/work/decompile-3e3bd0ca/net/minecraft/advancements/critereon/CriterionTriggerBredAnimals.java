package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerBredAnimals extends CriterionTriggerAbstract<CriterionTriggerBredAnimals.a> {

    public CriterionTriggerBredAnimals() {}

    @Override
    public Codec<CriterionTriggerBredAnimals.a> codec() {
        return CriterionTriggerBredAnimals.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, EntityAnimal entityanimal, EntityAnimal entityanimal1, @Nullable EntityAgeable entityageable) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityanimal);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityanimal1);
        LootTableInfo loottableinfo2 = entityageable != null ? CriterionConditionEntity.createContext(entityplayer, entityageable) : null;

        this.trigger(entityplayer, (criteriontriggerbredanimals_a) -> {
            return criteriontriggerbredanimals_a.matches(loottableinfo, loottableinfo1, loottableinfo2);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerBredAnimals.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerBredAnimals.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(CriterionTriggerBredAnimals.a::parent), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(CriterionTriggerBredAnimals.a::partner), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(CriterionTriggerBredAnimals.a::child)).apply(instance, CriterionTriggerBredAnimals.a::new);
        });

        public static Criterion<CriterionTriggerBredAnimals.a> bredAnimals() {
            return CriterionTriggers.BRED_ANIMALS.createCriterion(new CriterionTriggerBredAnimals.a(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerBredAnimals.a> bredAnimals(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.BRED_ANIMALS.createCriterion(new CriterionTriggerBredAnimals.a(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
        }

        public static Criterion<CriterionTriggerBredAnimals.a> bredAnimals(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionEntity> optional1, Optional<CriterionConditionEntity> optional2) {
            return CriterionTriggers.BRED_ANIMALS.createCriterion(new CriterionTriggerBredAnimals.a(Optional.empty(), CriterionConditionEntity.wrap(optional), CriterionConditionEntity.wrap(optional1), CriterionConditionEntity.wrap(optional2)));
        }

        public boolean matches(LootTableInfo loottableinfo, LootTableInfo loottableinfo1, @Nullable LootTableInfo loottableinfo2) {
            return this.child.isPresent() && (loottableinfo2 == null || !((ContextAwarePredicate) this.child.get()).matches(loottableinfo2)) ? false : matches(this.parent, loottableinfo) && matches(this.partner, loottableinfo1) || matches(this.parent, loottableinfo1) && matches(this.partner, loottableinfo);
        }

        private static boolean matches(Optional<ContextAwarePredicate> optional, LootTableInfo loottableinfo) {
            return optional.isEmpty() || ((ContextAwarePredicate) optional.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.parent, ".parent");
            criterionvalidator.validateEntity(this.partner, ".partner");
            criterionvalidator.validateEntity(this.child, ".child");
        }
    }
}
