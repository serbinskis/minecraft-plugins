package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerPlayerHurtEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerHurtEntity.a> {

    static final MinecraftKey ID = new MinecraftKey("player_hurt_entity");

    public CriterionTriggerPlayerHurtEntity() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerPlayerHurtEntity.ID;
    }

    @Override
    public CriterionTriggerPlayerHurtEntity.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionDamage criterionconditiondamage = CriterionConditionDamage.fromJson(jsonobject.get("damage"));
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerPlayerHurtEntity.a(contextawarepredicate, criterionconditiondamage, contextawarepredicate1);
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, DamageSource damagesource, float f, float f1, boolean flag) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerhurtentity_a) -> {
            return criteriontriggerplayerhurtentity_a.matches(entityplayer, loottableinfo, damagesource, f, f1, flag);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionDamage damage;
        private final ContextAwarePredicate entity;

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionDamage criterionconditiondamage, ContextAwarePredicate contextawarepredicate1) {
            super(CriterionTriggerPlayerHurtEntity.ID, contextawarepredicate);
            this.damage = criterionconditiondamage;
            this.entity = contextawarepredicate1;
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity() {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, CriterionConditionDamage.ANY, ContextAwarePredicate.ANY);
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity(CriterionConditionDamage criterionconditiondamage) {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, criterionconditiondamage, ContextAwarePredicate.ANY);
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity(CriterionConditionDamage.a criterionconditiondamage_a) {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, criterionconditiondamage_a.build(), ContextAwarePredicate.ANY);
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity(CriterionConditionEntity criterionconditionentity) {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, CriterionConditionDamage.ANY, CriterionConditionEntity.wrap(criterionconditionentity));
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity(CriterionConditionDamage criterionconditiondamage, CriterionConditionEntity criterionconditionentity) {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, criterionconditiondamage, CriterionConditionEntity.wrap(criterionconditionentity));
        }

        public static CriterionTriggerPlayerHurtEntity.a playerHurtEntity(CriterionConditionDamage.a criterionconditiondamage_a, CriterionConditionEntity criterionconditionentity) {
            return new CriterionTriggerPlayerHurtEntity.a(ContextAwarePredicate.ANY, criterionconditiondamage_a.build(), CriterionConditionEntity.wrap(criterionconditionentity));
        }

        public boolean matches(EntityPlayer entityplayer, LootTableInfo loottableinfo, DamageSource damagesource, float f, float f1, boolean flag) {
            return !this.damage.matches(entityplayer, damagesource, f, f1, flag) ? false : this.entity.matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("damage", this.damage.serializeToJson());
            jsonobject.add("entity", this.entity.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
