package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerChanneledLightning extends CriterionTriggerAbstract<CriterionTriggerChanneledLightning.a> {

    static final MinecraftKey ID = new MinecraftKey("channeled_lightning");

    public CriterionTriggerChanneledLightning() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerChanneledLightning.ID;
    }

    @Override
    public CriterionTriggerChanneledLightning.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        ContextAwarePredicate[] acontextawarepredicate = CriterionConditionEntity.fromJsonArray(jsonobject, "victims", lootdeserializationcontext);

        return new CriterionTriggerChanneledLightning.a(contextawarepredicate, acontextawarepredicate);
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

        private final ContextAwarePredicate[] victims;

        public a(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate[] acontextawarepredicate) {
            super(CriterionTriggerChanneledLightning.ID, contextawarepredicate);
            this.victims = acontextawarepredicate;
        }

        public static CriterionTriggerChanneledLightning.a channeledLightning(CriterionConditionEntity... acriterionconditionentity) {
            return new CriterionTriggerChanneledLightning.a(ContextAwarePredicate.ANY, (ContextAwarePredicate[]) Stream.of(acriterionconditionentity).map(CriterionConditionEntity::wrap).toArray((i) -> {
                return new ContextAwarePredicate[i];
            }));
        }

        public boolean matches(Collection<? extends LootTableInfo> collection) {
            ContextAwarePredicate[] acontextawarepredicate = this.victims;
            int i = acontextawarepredicate.length;
            int j = 0;

            while (j < i) {
                ContextAwarePredicate contextawarepredicate = acontextawarepredicate[j];
                boolean flag = false;
                Iterator iterator = collection.iterator();

                while (true) {
                    if (iterator.hasNext()) {
                        LootTableInfo loottableinfo = (LootTableInfo) iterator.next();

                        if (!contextawarepredicate.matches(loottableinfo)) {
                            continue;
                        }

                        flag = true;
                    }

                    if (!flag) {
                        return false;
                    }

                    ++j;
                    break;
                }
            }

            return true;
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, lootserializationcontext));
            return jsonobject;
        }
    }
}
