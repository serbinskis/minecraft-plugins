package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.phys.Vec3D;

public class CriterionTriggerTargetHit extends CriterionTriggerAbstract<CriterionTriggerTargetHit.a> {

    public CriterionTriggerTargetHit() {}

    @Override
    public CriterionTriggerTargetHit.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("signal_strength"));
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "projectile", lootdeserializationcontext);

        return new CriterionTriggerTargetHit.a(optional, criterionconditionvalue_integerrange, optional1);
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, Vec3D vec3d, int i) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggertargethit_a) -> {
            return criteriontriggertargethit_a.matches(loottableinfo, vec3d, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionValue.IntegerRange signalStrength;
        private final Optional<ContextAwarePredicate> projectile;

        public a(Optional<ContextAwarePredicate> optional, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, Optional<ContextAwarePredicate> optional1) {
            super(optional);
            this.signalStrength = criterionconditionvalue_integerrange;
            this.projectile = optional1;
        }

        public static Criterion<CriterionTriggerTargetHit.a> targetHit(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, Optional<ContextAwarePredicate> optional) {
            return CriterionTriggers.TARGET_BLOCK_HIT.createCriterion(new CriterionTriggerTargetHit.a(Optional.empty(), criterionconditionvalue_integerrange, optional));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.add("signal_strength", this.signalStrength.serializeToJson());
            this.projectile.ifPresent((contextawarepredicate) -> {
                jsonobject.add("projectile", contextawarepredicate.toJson());
            });
            return jsonobject;
        }

        public boolean matches(LootTableInfo loottableinfo, Vec3D vec3d, int i) {
            return !this.signalStrength.matches(i) ? false : !this.projectile.isPresent() || ((ContextAwarePredicate) this.projectile.get()).matches(loottableinfo);
        }
    }
}
