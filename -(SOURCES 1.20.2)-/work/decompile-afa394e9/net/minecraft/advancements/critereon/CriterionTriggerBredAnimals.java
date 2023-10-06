package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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
    public CriterionTriggerBredAnimals.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "parent", lootdeserializationcontext);
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "partner", lootdeserializationcontext);
        Optional<ContextAwarePredicate> optional3 = CriterionConditionEntity.fromJson(jsonobject, "child", lootdeserializationcontext);

        return new CriterionTriggerBredAnimals.a(optional, optional1, optional2, optional3);
    }

    public void trigger(EntityPlayer entityplayer, EntityAnimal entityanimal, EntityAnimal entityanimal1, @Nullable EntityAgeable entityageable) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityanimal);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityanimal1);
        LootTableInfo loottableinfo2 = entityageable != null ? CriterionConditionEntity.createContext(entityplayer, entityageable) : null;

        this.trigger(entityplayer, (criteriontriggerbredanimals_a) -> {
            return criteriontriggerbredanimals_a.matches(loottableinfo, loottableinfo1, loottableinfo2);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> parent;
        private final Optional<ContextAwarePredicate> partner;
        private final Optional<ContextAwarePredicate> child;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1, Optional<ContextAwarePredicate> optional2, Optional<ContextAwarePredicate> optional3) {
            super(optional);
            this.parent = optional1;
            this.partner = optional2;
            this.child = optional3;
        }

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
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.parent.ifPresent((contextawarepredicate) -> {
                jsonobject.add("parent", contextawarepredicate.toJson());
            });
            this.partner.ifPresent((contextawarepredicate) -> {
                jsonobject.add("partner", contextawarepredicate.toJson());
            });
            this.child.ifPresent((contextawarepredicate) -> {
                jsonobject.add("child", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
