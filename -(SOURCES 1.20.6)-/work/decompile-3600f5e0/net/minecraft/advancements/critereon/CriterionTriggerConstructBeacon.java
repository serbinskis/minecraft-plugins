package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;

public class CriterionTriggerConstructBeacon extends CriterionTriggerAbstract<CriterionTriggerConstructBeacon.a> {

    public CriterionTriggerConstructBeacon() {}

    @Override
    public Codec<CriterionTriggerConstructBeacon.a> codec() {
        return CriterionTriggerConstructBeacon.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, int i) {
        this.trigger(entityplayer, (criteriontriggerconstructbeacon_a) -> {
            return criteriontriggerconstructbeacon_a.matches(i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, CriterionConditionValue.IntegerRange level) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerConstructBeacon.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerConstructBeacon.a::player), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("level", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerConstructBeacon.a::level)).apply(instance, CriterionTriggerConstructBeacon.a::new);
        });

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon() {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), criterionconditionvalue_integerrange));
        }

        public boolean matches(int i) {
            return this.level.matches(i);
        }
    }
}
