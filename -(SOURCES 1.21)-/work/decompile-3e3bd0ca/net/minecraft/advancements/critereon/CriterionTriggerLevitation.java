package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.phys.Vec3D;

public class CriterionTriggerLevitation extends CriterionTriggerAbstract<CriterionTriggerLevitation.a> {

    public CriterionTriggerLevitation() {}

    @Override
    public Codec<CriterionTriggerLevitation.a> codec() {
        return CriterionTriggerLevitation.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Vec3D vec3d, int i) {
        this.trigger(entityplayer, (criteriontriggerlevitation_a) -> {
            return criteriontriggerlevitation_a.matches(entityplayer, vec3d, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionDistance> distance, CriterionConditionValue.IntegerRange duration) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerLevitation.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerLevitation.a::player), CriterionConditionDistance.CODEC.optionalFieldOf("distance").forGetter(CriterionTriggerLevitation.a::distance), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("duration", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerLevitation.a::duration)).apply(instance, CriterionTriggerLevitation.a::new);
        });

        public static Criterion<CriterionTriggerLevitation.a> levitated(CriterionConditionDistance criterionconditiondistance) {
            return CriterionTriggers.LEVITATION.createCriterion(new CriterionTriggerLevitation.a(Optional.empty(), Optional.of(criterionconditiondistance), CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(EntityPlayer entityplayer, Vec3D vec3d, int i) {
            return this.distance.isPresent() && !((CriterionConditionDistance) this.distance.get()).matches(vec3d.x, vec3d.y, vec3d.z, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ()) ? false : this.duration.matches(i);
        }
    }
}
