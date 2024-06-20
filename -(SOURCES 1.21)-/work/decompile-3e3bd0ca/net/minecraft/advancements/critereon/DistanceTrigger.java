package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.Vec3D;

public class DistanceTrigger extends CriterionTriggerAbstract<DistanceTrigger.a> {

    public DistanceTrigger() {}

    @Override
    public Codec<DistanceTrigger.a> codec() {
        return DistanceTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Vec3D vec3d) {
        Vec3D vec3d1 = entityplayer.position();

        this.trigger(entityplayer, (distancetrigger_a) -> {
            return distancetrigger_a.matches(entityplayer.serverLevel(), vec3d, vec3d1);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionLocation> startPosition, Optional<CriterionConditionDistance> distance) implements CriterionTriggerAbstract.a {

        public static final Codec<DistanceTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DistanceTrigger.a::player), CriterionConditionLocation.CODEC.optionalFieldOf("start_position").forGetter(DistanceTrigger.a::startPosition), CriterionConditionDistance.CODEC.optionalFieldOf("distance").forGetter(DistanceTrigger.a::distance)).apply(instance, DistanceTrigger.a::new);
        });

        public static Criterion<DistanceTrigger.a> fallFromHeight(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDistance criterionconditiondistance, CriterionConditionLocation.a criterionconditionlocation_a) {
            return CriterionTriggers.FALL_FROM_HEIGHT.createCriterion(new DistanceTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditionlocation_a.build()), Optional.of(criterionconditiondistance)));
        }

        public static Criterion<DistanceTrigger.a> rideEntityInLava(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.createCriterion(new DistanceTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty(), Optional.of(criterionconditiondistance)));
        }

        public static Criterion<DistanceTrigger.a> travelledThroughNether(CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.NETHER_TRAVEL.createCriterion(new DistanceTrigger.a(Optional.empty(), Optional.empty(), Optional.of(criterionconditiondistance)));
        }

        public boolean matches(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1) {
            return this.startPosition.isPresent() && !((CriterionConditionLocation) this.startPosition.get()).matches(worldserver, vec3d.x, vec3d.y, vec3d.z) ? false : !this.distance.isPresent() || ((CriterionConditionDistance) this.distance.get()).matches(vec3d.x, vec3d.y, vec3d.z, vec3d1.x, vec3d1.y, vec3d1.z);
        }
    }
}
