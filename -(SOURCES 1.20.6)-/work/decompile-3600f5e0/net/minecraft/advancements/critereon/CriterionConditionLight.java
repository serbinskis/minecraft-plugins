package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;

public record CriterionConditionLight(CriterionConditionValue.IntegerRange composite) {

    public static final Codec<CriterionConditionLight> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("light", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionLight::composite)).apply(instance, CriterionConditionLight::new);
    });

    public boolean matches(WorldServer worldserver, BlockPosition blockposition) {
        return !worldserver.isLoaded(blockposition) ? false : this.composite.matches(worldserver.getMaxLocalRawBrightness(blockposition));
    }

    public static class a {

        private CriterionConditionValue.IntegerRange composite;

        public a() {
            this.composite = CriterionConditionValue.IntegerRange.ANY;
        }

        public static CriterionConditionLight.a light() {
            return new CriterionConditionLight.a();
        }

        public CriterionConditionLight.a setComposite(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.composite = criterionconditionvalue_integerrange;
            return this;
        }

        public CriterionConditionLight build() {
            return new CriterionConditionLight(this.composite);
        }
    }
}
