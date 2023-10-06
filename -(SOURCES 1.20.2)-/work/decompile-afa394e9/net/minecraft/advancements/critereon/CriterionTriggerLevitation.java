package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.phys.Vec3D;

public class CriterionTriggerLevitation extends CriterionTriggerAbstract<CriterionTriggerLevitation.a> {

    public CriterionTriggerLevitation() {}

    @Override
    public CriterionTriggerLevitation.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionDistance> optional1 = CriterionConditionDistance.fromJson(jsonobject.get("distance"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("duration"));

        return new CriterionTriggerLevitation.a(optional, optional1, criterionconditionvalue_integerrange);
    }

    public void trigger(EntityPlayer entityplayer, Vec3D vec3d, int i) {
        this.trigger(entityplayer, (criteriontriggerlevitation_a) -> {
            return criteriontriggerlevitation_a.matches(entityplayer, vec3d, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionDistance> distance;
        private final CriterionConditionValue.IntegerRange duration;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionDistance> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            super(optional);
            this.distance = optional1;
            this.duration = criterionconditionvalue_integerrange;
        }

        public static Criterion<CriterionTriggerLevitation.a> levitated(CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.LEVITATION.createCriterion(new CriterionTriggerLevitation.a(Optional.empty(), Optional.of(criterionconditiondistance), CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(EntityPlayer entityplayer, Vec3D vec3d, int i) {
            return this.distance.isPresent() && !((CriterionConditionDistance) this.distance.get()).matches(vec3d.x, vec3d.y, vec3d.z, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ()) ? false : this.duration.matches(i);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.distance.ifPresent((criterionconditiondistance) -> {
                jsonobject.add("distance", criterionconditiondistance.serializeToJson());
            });
            jsonobject.add("duration", this.duration.serializeToJson());
            return jsonobject;
        }
    }
}
