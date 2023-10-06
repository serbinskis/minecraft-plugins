package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerTamedAnimal extends CriterionTriggerAbstract<CriterionTriggerTamedAnimal.a> {

    public CriterionTriggerTamedAnimal() {}

    @Override
    public CriterionTriggerTamedAnimal.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerTamedAnimal.a(optional, optional1);
    }

    public void trigger(EntityPlayer entityplayer, EntityAnimal entityanimal) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityanimal);

        this.trigger(entityplayer, (criteriontriggertamedanimal_a) -> {
            return criteriontriggertamedanimal_a.matches(loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> entity;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1) {
            super(optional);
            this.entity = optional1;
        }

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
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.entity.ifPresent((contextawarepredicate) -> {
                jsonobject.add("entity", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
