package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.MathHelper;

public record MovementPredicate(CriterionConditionValue.DoubleRange x, CriterionConditionValue.DoubleRange y, CriterionConditionValue.DoubleRange z, CriterionConditionValue.DoubleRange speed, CriterionConditionValue.DoubleRange horizontalSpeed, CriterionConditionValue.DoubleRange verticalSpeed, CriterionConditionValue.DoubleRange fallDistance) {

    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("x", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::x), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("y", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::y), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("z", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::z), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("speed", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::speed), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("horizontal_speed", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::horizontalSpeed), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("vertical_speed", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::verticalSpeed), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("fall_distance", CriterionConditionValue.DoubleRange.ANY).forGetter(MovementPredicate::fallDistance)).apply(instance, MovementPredicate::new);
    });

    public static MovementPredicate speed(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new MovementPredicate(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY);
    }

    public static MovementPredicate horizontalSpeed(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new MovementPredicate(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY);
    }

    public static MovementPredicate verticalSpeed(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new MovementPredicate(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange.ANY);
    }

    public static MovementPredicate fallDistance(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new MovementPredicate(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange);
    }

    public boolean matches(double d0, double d1, double d2, double d3) {
        if (this.x.matches(d0) && this.y.matches(d1) && this.z.matches(d2)) {
            double d4 = MathHelper.lengthSquared(d0, d1, d2);

            if (!this.speed.matchesSqr(d4)) {
                return false;
            } else {
                double d5 = MathHelper.lengthSquared(d0, d2);

                if (!this.horizontalSpeed.matchesSqr(d5)) {
                    return false;
                } else {
                    double d6 = Math.abs(d1);

                    return !this.verticalSpeed.matches(d6) ? false : this.fallDistance.matches(d3);
                }
            }
        } else {
            return false;
        }
    }
}
