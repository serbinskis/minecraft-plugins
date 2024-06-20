package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;

public class CriterionTriggerUsedEnderEye extends CriterionTriggerAbstract<CriterionTriggerUsedEnderEye.a> {

    public CriterionTriggerUsedEnderEye() {}

    @Override
    public Codec<CriterionTriggerUsedEnderEye.a> codec() {
        return CriterionTriggerUsedEnderEye.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, BlockPosition blockposition) {
        double d0 = entityplayer.getX() - (double) blockposition.getX();
        double d1 = entityplayer.getZ() - (double) blockposition.getZ();
        double d2 = d0 * d0 + d1 * d1;

        this.trigger(entityplayer, (criteriontriggerusedendereye_a) -> {
            return criteriontriggerusedendereye_a.matches(d2);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, CriterionConditionValue.DoubleRange distance) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerUsedEnderEye.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerUsedEnderEye.a::player), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("distance", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionTriggerUsedEnderEye.a::distance)).apply(instance, CriterionTriggerUsedEnderEye.a::new);
        });

        public boolean matches(double d0) {
            return this.distance.matchesSqr(d0);
        }
    }
}
