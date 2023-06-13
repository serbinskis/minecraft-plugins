package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class LightningStrikeTrigger extends CriterionTriggerAbstract<LightningStrikeTrigger.a> {

    static final MinecraftKey ID = new MinecraftKey("lightning_strike");

    public LightningStrikeTrigger() {}

    @Override
    public MinecraftKey getId() {
        return LightningStrikeTrigger.ID;
    }

    @Override
    public LightningStrikeTrigger.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "lightning", lootdeserializationcontext);
        ContextAwarePredicate contextawarepredicate2 = CriterionConditionEntity.fromJson(jsonobject, "bystander", lootdeserializationcontext);

        return new LightningStrikeTrigger.a(contextawarepredicate, contextawarepredicate1, contextawarepredicate2);
    }

    public void trigger(EntityPlayer entityplayer, EntityLightning entitylightning, List<Entity> list) {
        List<LootTableInfo> list1 = (List) list.stream().map((entity) -> {
            return CriterionConditionEntity.createContext(entityplayer, entity);
        }).collect(Collectors.toList());
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entitylightning);

        this.trigger(entityplayer, (lightningstriketrigger_a) -> {
            return lightningstriketrigger_a.matches(loottableinfo, list1);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final ContextAwarePredicate lightning;
        private final ContextAwarePredicate bystander;

        public a(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2) {
            super(LightningStrikeTrigger.ID, contextawarepredicate);
            this.lightning = contextawarepredicate1;
            this.bystander = contextawarepredicate2;
        }

        public static LightningStrikeTrigger.a lighthingStrike(CriterionConditionEntity criterionconditionentity, CriterionConditionEntity criterionconditionentity1) {
            return new LightningStrikeTrigger.a(ContextAwarePredicate.ANY, CriterionConditionEntity.wrap(criterionconditionentity), CriterionConditionEntity.wrap(criterionconditionentity1));
        }

        public boolean matches(LootTableInfo loottableinfo, List<LootTableInfo> list) {
            if (!this.lightning.matches(loottableinfo)) {
                return false;
            } else {
                if (this.bystander != ContextAwarePredicate.ANY) {
                    Stream stream = list.stream();
                    ContextAwarePredicate contextawarepredicate = this.bystander;

                    Objects.requireNonNull(this.bystander);
                    if (stream.noneMatch(contextawarepredicate::matches)) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("lightning", this.lightning.toJson(lootserializationcontext));
            jsonobject.add("bystander", this.bystander.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
