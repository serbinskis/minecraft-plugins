package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerPlayerHurtEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerHurtEntity.a> {

    public CriterionTriggerPlayerHurtEntity() {}

    @Override
    public CriterionTriggerPlayerHurtEntity.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionDamage> optional1 = CriterionConditionDamage.fromJson(jsonobject.get("damage"));
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerPlayerHurtEntity.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, DamageSource damagesource, float f, float f1, boolean flag) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerhurtentity_a) -> {
            return criteriontriggerplayerhurtentity_a.matches(entityplayer, loottableinfo, damagesource, f, f1, flag);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionDamage> damage;
        private final Optional<ContextAwarePredicate> entity;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionDamage> optional1, Optional<ContextAwarePredicate> optional2) {
            super(optional);
            this.damage = optional1;
            this.entity = optional2;
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity() {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntityWithDamage(Optional<CriterionConditionDamage> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), optional, Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntityWithDamage(CriterionConditionDamage.a criterionconditiondamage_a) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build()), Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.empty(), CriterionConditionEntity.wrap(optional)));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(Optional<CriterionConditionDamage> optional, Optional<CriterionConditionEntity> optional1) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), optional, CriterionConditionEntity.wrap(optional1)));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(CriterionConditionDamage.a criterionconditiondamage_a, Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build()), CriterionConditionEntity.wrap(optional)));
        }

        public boolean matches(EntityPlayer entityplayer, LootTableInfo loottableinfo, DamageSource damagesource, float f, float f1, boolean flag) {
            return this.damage.isPresent() && !((CriterionConditionDamage) this.damage.get()).matches(entityplayer, damagesource, f, f1, flag) ? false : !this.entity.isPresent() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.damage.ifPresent((criterionconditiondamage) -> {
                jsonobject.add("damage", criterionconditiondamage.serializeToJson());
            });
            this.entity.ifPresent((contextawarepredicate) -> {
                jsonobject.add("entity", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
