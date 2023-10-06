package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerChanneledLightning extends CriterionTriggerAbstract<CriterionTriggerChanneledLightning.a> {

    public CriterionTriggerChanneledLightning() {}

    @Override
    public CriterionTriggerChanneledLightning.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        List<ContextAwarePredicate> list = CriterionConditionEntity.fromJsonArray(jsonobject, "victims", lootdeserializationcontext);

        return new CriterionTriggerChanneledLightning.a(optional, list);
    }

    public void trigger(EntityPlayer entityplayer, Collection<? extends Entity> collection) {
        List<LootTableInfo> list = (List) collection.stream().map((entity) -> {
            return CriterionConditionEntity.createContext(entityplayer, entity);
        }).collect(Collectors.toList());

        this.trigger(entityplayer, (criteriontriggerchanneledlightning_a) -> {
            return criteriontriggerchanneledlightning_a.matches(list);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final List<ContextAwarePredicate> victims;

        public a(Optional<ContextAwarePredicate> optional, List<ContextAwarePredicate> list) {
            super(optional);
            this.victims = list;
        }

        public static Criterion<CriterionTriggerChanneledLightning.a> channeledLightning(CriterionConditionEntity.a... acriterionconditionentity_a) {
            return CriterionTriggers.CHANNELED_LIGHTNING.createCriterion(new CriterionTriggerChanneledLightning.a(Optional.empty(), CriterionConditionEntity.wrap(acriterionconditionentity_a)));
        }

        public boolean matches(Collection<? extends LootTableInfo> collection) {
            Iterator iterator = this.victims.iterator();

            while (iterator.hasNext()) {
                ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();
                boolean flag = false;
                Iterator iterator1 = collection.iterator();

                while (true) {
                    if (iterator1.hasNext()) {
                        LootTableInfo loottableinfo = (LootTableInfo) iterator1.next();

                        if (!contextawarepredicate.matches(loottableinfo)) {
                            continue;
                        }

                        flag = true;
                    }

                    if (!flag) {
                        return false;
                    }
                    break;
                }
            }

            return true;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims));
            return jsonobject;
        }
    }
}
