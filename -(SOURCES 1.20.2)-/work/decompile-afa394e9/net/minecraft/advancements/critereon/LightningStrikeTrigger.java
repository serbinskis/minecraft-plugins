package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class LightningStrikeTrigger extends CriterionTriggerAbstract<LightningStrikeTrigger.a> {

    public LightningStrikeTrigger() {}

    @Override
    public LightningStrikeTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "lightning", lootdeserializationcontext);
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "bystander", lootdeserializationcontext);

        return new LightningStrikeTrigger.a(optional, optional1, optional2);
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

        private final Optional<ContextAwarePredicate> lightning;
        private final Optional<ContextAwarePredicate> bystander;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1, Optional<ContextAwarePredicate> optional2) {
            super(optional);
            this.lightning = optional1;
            this.bystander = optional2;
        }

        public static Criterion<LightningStrikeTrigger.a> lightningStrike(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionEntity> optional1) {
            return CriterionTriggers.LIGHTNING_STRIKE.createCriterion(new LightningStrikeTrigger.a(Optional.empty(), CriterionConditionEntity.wrap(optional), CriterionConditionEntity.wrap(optional1)));
        }

        public boolean matches(LootTableInfo loottableinfo, List<LootTableInfo> list) {
            if (this.lightning.isPresent() && !((ContextAwarePredicate) this.lightning.get()).matches(loottableinfo)) {
                return false;
            } else {
                if (this.bystander.isPresent()) {
                    Stream stream = list.stream();
                    ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) this.bystander.get();

                    Objects.requireNonNull(contextawarepredicate);
                    if (stream.noneMatch(contextawarepredicate::matches)) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.lightning.ifPresent((contextawarepredicate) -> {
                jsonobject.add("lightning", contextawarepredicate.toJson());
            });
            this.bystander.ifPresent((contextawarepredicate) -> {
                jsonobject.add("bystander", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
