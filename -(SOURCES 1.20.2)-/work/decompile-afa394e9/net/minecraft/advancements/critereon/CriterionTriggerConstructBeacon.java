package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;

public class CriterionTriggerConstructBeacon extends CriterionTriggerAbstract<CriterionTriggerConstructBeacon.a> {

    public CriterionTriggerConstructBeacon() {}

    @Override
    public CriterionTriggerConstructBeacon.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("level"));

        return new CriterionTriggerConstructBeacon.a(optional, criterionconditionvalue_integerrange);
    }

    public void trigger(EntityPlayer entityplayer, int i) {
        this.trigger(entityplayer, (criteriontriggerconstructbeacon_a) -> {
            return criteriontriggerconstructbeacon_a.matches(i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionValue.IntegerRange level;

        public a(Optional<ContextAwarePredicate> optional, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            super(optional);
            this.level = criterionconditionvalue_integerrange;
        }

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon() {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), criterionconditionvalue_integerrange));
        }

        public boolean matches(int i) {
            return this.level.matches(i);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            jsonobject.add("level", this.level.serializeToJson());
            return jsonobject;
        }
    }
}
