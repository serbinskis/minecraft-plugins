package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerSummonedEntity extends CriterionTriggerAbstract<CriterionTriggerSummonedEntity.a> {

    public CriterionTriggerSummonedEntity() {}

    @Override
    public CriterionTriggerSummonedEntity.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerSummonedEntity.a(optional, optional1);
    }

    public void trigger(EntityPlayer entityplayer, Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggersummonedentity_a) -> {
            return criteriontriggersummonedentity_a.matches(loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> entity;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1) {
            super(optional);
            this.entity = optional1;
        }

        public static Criterion<CriterionTriggerSummonedEntity.a> summonedEntity(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.SUMMONED_ENTITY.createCriterion(new CriterionTriggerSummonedEntity.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
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
