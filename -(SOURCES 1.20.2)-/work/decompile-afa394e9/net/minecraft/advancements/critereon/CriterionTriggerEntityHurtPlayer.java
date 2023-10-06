package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class CriterionTriggerEntityHurtPlayer extends CriterionTriggerAbstract<CriterionTriggerEntityHurtPlayer.a> {

    public CriterionTriggerEntityHurtPlayer() {}

    @Override
    public CriterionTriggerEntityHurtPlayer.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionDamage> optional1 = CriterionConditionDamage.fromJson(jsonobject.get("damage"));

        return new CriterionTriggerEntityHurtPlayer.a(optional, optional1);
    }

    public void trigger(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
        this.trigger(entityplayer, (criteriontriggerentityhurtplayer_a) -> {
            return criteriontriggerentityhurtplayer_a.matches(entityplayer, damagesource, f, f1, flag);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionDamage> damage;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionDamage> optional1) {
            super(optional);
            this.damage = optional1;
        }

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer() {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer(CriterionConditionDamage criterionconditiondamage) {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.of(criterionconditiondamage)));
        }

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer(CriterionConditionDamage.a criterionconditiondamage_a) {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build())));
        }

        public boolean matches(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
            return !this.damage.isPresent() || ((CriterionConditionDamage) this.damage.get()).matches(entityplayer, damagesource, f, f1, flag);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.damage.ifPresent((criterionconditiondamage) -> {
                jsonobject.add("damage", criterionconditiondamage.serializeToJson());
            });
            return jsonobject;
        }
    }
}
