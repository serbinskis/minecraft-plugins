package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.phys.Vec3D;

public class CriterionTriggerTargetHit extends CriterionTriggerAbstract<CriterionTriggerTargetHit.a> {

    static final MinecraftKey ID = new MinecraftKey("target_hit");

    public CriterionTriggerTargetHit() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerTargetHit.ID;
    }

    @Override
    public CriterionTriggerTargetHit.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("signal_strength"));
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "projectile", lootdeserializationcontext);

        return new CriterionTriggerTargetHit.a(contextawarepredicate, criterionconditionvalue_integerrange, contextawarepredicate1);
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, Vec3D vec3d, int i) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggertargethit_a) -> {
            return criteriontriggertargethit_a.matches(loottableinfo, vec3d, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionValue.IntegerRange signalStrength;
        private final ContextAwarePredicate projectile;

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, ContextAwarePredicate contextawarepredicate1) {
            super(CriterionTriggerTargetHit.ID, contextawarepredicate);
            this.signalStrength = criterionconditionvalue_integerrange;
            this.projectile = contextawarepredicate1;
        }

        public static CriterionTriggerTargetHit.a targetHit(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, ContextAwarePredicate contextawarepredicate) {
            return new CriterionTriggerTargetHit.a(ContextAwarePredicate.ANY, criterionconditionvalue_integerrange, contextawarepredicate);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("signal_strength", this.signalStrength.serializeToJson());
            jsonobject.add("projectile", this.projectile.toJson(lootserializationcontext));
            return jsonobject;
        }

        public boolean matches(LootTableInfo loottableinfo, Vec3D vec3d, int i) {
            return !this.signalStrength.matches(i) ? false : this.projectile.matches(loottableinfo);
        }
    }
}
