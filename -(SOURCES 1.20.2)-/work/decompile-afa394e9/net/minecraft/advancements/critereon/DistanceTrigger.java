package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.Vec3D;

public class DistanceTrigger extends CriterionTriggerAbstract<DistanceTrigger.a> {

    public DistanceTrigger() {}

    @Override
    public DistanceTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionLocation> optional1 = CriterionConditionLocation.fromJson(jsonobject.get("start_position"));
        Optional<CriterionConditionDistance> optional2 = CriterionConditionDistance.fromJson(jsonobject.get("distance"));

        return new DistanceTrigger.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, Vec3D vec3d) {
        Vec3D vec3d1 = entityplayer.position();

        this.trigger(entityplayer, (distancetrigger_a) -> {
            return distancetrigger_a.matches(entityplayer.serverLevel(), vec3d, vec3d1);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionLocation> startPosition;
        private final Optional<CriterionConditionDistance> distance;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionLocation> optional1, Optional<CriterionConditionDistance> optional2) {
            super(optional);
            this.startPosition = optional1;
            this.distance = optional2;
        }

        public static Criterion<DistanceTrigger.a> fallFromHeight(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDistance criterionconditiondistance, CriterionConditionLocation.a criterionconditionlocation_a) {
            return CriterionTriggers.FALL_FROM_HEIGHT.createCriterion(new DistanceTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditionlocation_a.build()), Optional.of(criterionconditiondistance)));
        }

        public static Criterion<DistanceTrigger.a> rideEntityInLava(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.createCriterion(new DistanceTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty(), Optional.of(criterionconditiondistance)));
        }

        public static Criterion<DistanceTrigger.a> travelledThroughNether(CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.NETHER_TRAVEL.createCriterion(new DistanceTrigger.a(Optional.empty(), Optional.empty(), Optional.of(criterionconditiondistance)));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.startPosition.ifPresent((criterionconditionlocation) -> {
                jsonobject.add("start_position", criterionconditionlocation.serializeToJson());
            });
            this.distance.ifPresent((criterionconditiondistance) -> {
                jsonobject.add("distance", criterionconditiondistance.serializeToJson());
            });
            return jsonobject;
        }

        public boolean matches(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1) {
            return this.startPosition.isPresent() && !((CriterionConditionLocation) this.startPosition.get()).matches(worldserver, vec3d.x, vec3d.y, vec3d.z) ? false : !this.distance.isPresent() || ((CriterionConditionDistance) this.distance.get()).matches(vec3d.x, vec3d.y, vec3d.z, vec3d1.x, vec3d1.y, vec3d1.z);
        }
    }
}
