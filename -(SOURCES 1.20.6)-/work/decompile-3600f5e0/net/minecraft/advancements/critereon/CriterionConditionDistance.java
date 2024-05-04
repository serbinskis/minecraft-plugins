package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.MathHelper;

public record CriterionConditionDistance(CriterionConditionValue.DoubleRange x, CriterionConditionValue.DoubleRange y, CriterionConditionValue.DoubleRange z, CriterionConditionValue.DoubleRange horizontal, CriterionConditionValue.DoubleRange absolute) {

    public static final Codec<CriterionConditionDistance> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("x", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDistance::x), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("y", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDistance::y), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("z", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDistance::z), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("horizontal", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDistance::horizontal), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("absolute", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionDistance::absolute)).apply(instance, CriterionConditionDistance::new);
    });

    public static CriterionConditionDistance horizontal(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new CriterionConditionDistance(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange.ANY);
    }

    public static CriterionConditionDistance vertical(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new CriterionConditionDistance(CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY);
    }

    public static CriterionConditionDistance absolute(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
        return new CriterionConditionDistance(CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, CriterionConditionValue.DoubleRange.ANY, criterionconditionvalue_doublerange);
    }

    public boolean matches(double d0, double d1, double d2, double d3, double d4, double d5) {
        float f = (float) (d0 - d3);
        float f1 = (float) (d1 - d4);
        float f2 = (float) (d2 - d5);

        return this.x.matches((double) MathHelper.abs(f)) && this.y.matches((double) MathHelper.abs(f1)) && this.z.matches((double) MathHelper.abs(f2)) ? (!this.horizontal.matchesSqr((double) (f * f + f2 * f2)) ? false : this.absolute.matchesSqr((double) (f * f + f1 * f1 + f2 * f2))) : false;
    }
}
