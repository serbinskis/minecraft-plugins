package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerKilled extends CriterionTriggerAbstract<CriterionTriggerKilled.a> {

    public CriterionTriggerKilled() {}

    @Override
    public CriterionTriggerKilled.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        return new CriterionTriggerKilled.a(optional, CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext), CriterionConditionDamageSource.fromJson(jsonobject.get("killing_blow")));
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, DamageSource damagesource) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerkilled_a) -> {
            return criteriontriggerkilled_a.matches(entityplayer, loottableinfo, damagesource);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> entityPredicate;
        private final Optional<CriterionConditionDamageSource> killingBlow;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1, Optional<CriterionConditionDamageSource> optional2) {
            super(optional);
            this.entityPredicate = optional1;
            this.killingBlow = optional2;
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity() {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionDamageSource> optional1) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), optional1));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a, Optional<CriterionConditionDamageSource> optional) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), optional));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntityNearSculkCatalyst() {
            return CriterionTriggers.KILL_MOB_NEAR_SCULK_CATALYST.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer() {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionDamageSource> optional1) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), optional1));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a, Optional<CriterionConditionDamageSource> optional) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), optional));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public boolean matches(EntityPlayer entityplayer, LootTableInfo loottableinfo, DamageSource damagesource) {
            return this.killingBlow.isPresent() && !((CriterionConditionDamageSource) this.killingBlow.get()).matches(entityplayer, damagesource) ? false : this.entityPredicate.isEmpty() || ((ContextAwarePredicate) this.entityPredicate.get()).matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.entityPredicate.ifPresent((contextawarepredicate) -> {
                jsonobject.add("entity", contextawarepredicate.toJson());
            });
            this.killingBlow.ifPresent((criterionconditiondamagesource) -> {
                jsonobject.add("killing_blow", criterionconditiondamagesource.serializeToJson());
            });
            return jsonobject;
        }
    }
}
