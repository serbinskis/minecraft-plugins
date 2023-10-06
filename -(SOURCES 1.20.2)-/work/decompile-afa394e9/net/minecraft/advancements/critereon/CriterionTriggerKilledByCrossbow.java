package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerKilledByCrossbow extends CriterionTriggerAbstract<CriterionTriggerKilledByCrossbow.a> {

    public CriterionTriggerKilledByCrossbow() {}

    @Override
    public CriterionTriggerKilledByCrossbow.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        List<ContextAwarePredicate> list = CriterionConditionEntity.fromJsonArray(jsonobject, "victims", lootdeserializationcontext);
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("unique_entity_types"));

        return new CriterionTriggerKilledByCrossbow.a(optional, list, criterionconditionvalue_integerrange);
    }

    public void trigger(EntityPlayer entityplayer, Collection<Entity> collection) {
        List<LootTableInfo> list = Lists.newArrayList();
        Set<EntityTypes<?>> set = Sets.newHashSet();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            set.add(entity.getType());
            list.add(CriterionConditionEntity.createContext(entityplayer, entity));
        }

        this.trigger(entityplayer, (criteriontriggerkilledbycrossbow_a) -> {
            return criteriontriggerkilledbycrossbow_a.matches(list, set.size());
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final List<ContextAwarePredicate> victims;
        private final CriterionConditionValue.IntegerRange uniqueEntityTypes;

        public a(Optional<ContextAwarePredicate> optional, List<ContextAwarePredicate> list, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            super(optional);
            this.victims = list;
            this.uniqueEntityTypes = criterionconditionvalue_integerrange;
        }

        public static Criterion<CriterionTriggerKilledByCrossbow.a> crossbowKilled(CriterionConditionEntity.a... acriterionconditionentity_a) {
            return CriterionTriggers.KILLED_BY_CROSSBOW.createCriterion(new CriterionTriggerKilledByCrossbow.a(Optional.empty(), CriterionConditionEntity.wrap(acriterionconditionentity_a), CriterionConditionValue.IntegerRange.ANY));
        }

        public static Criterion<CriterionTriggerKilledByCrossbow.a> crossbowKilled(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.KILLED_BY_CROSSBOW.createCriterion(new CriterionTriggerKilledByCrossbow.a(Optional.empty(), List.of(), criterionconditionvalue_integerrange));
        }

        public boolean matches(Collection<LootTableInfo> collection, int i) {
            if (!this.victims.isEmpty()) {
                List<LootTableInfo> list = Lists.newArrayList(collection);
                Iterator iterator = this.victims.iterator();

                while (iterator.hasNext()) {
                    ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();
                    boolean flag = false;
                    Iterator iterator1 = list.iterator();

                    while (iterator1.hasNext()) {
                        LootTableInfo loottableinfo = (LootTableInfo) iterator1.next();

                        if (contextawarepredicate.matches(loottableinfo)) {
                            iterator1.remove();
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }

            return this.uniqueEntityTypes.matches(i);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims));
            jsonobject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
            return jsonobject;
        }
    }
}
