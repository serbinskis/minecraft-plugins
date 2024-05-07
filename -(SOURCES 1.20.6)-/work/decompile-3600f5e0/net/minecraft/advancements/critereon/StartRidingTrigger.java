package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;

public class StartRidingTrigger extends CriterionTriggerAbstract<StartRidingTrigger.a> {

    public StartRidingTrigger() {}

    @Override
    public Codec<StartRidingTrigger.a> codec() {
        return StartRidingTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer) {
        this.trigger(entityplayer, (startridingtrigger_a) -> {
            return true;
        });
    }

    public static record a(Optional<ContextAwarePredicate> player) implements CriterionTriggerAbstract.a {

        public static final Codec<StartRidingTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(StartRidingTrigger.a::player)).apply(instance, StartRidingTrigger.a::new);
        });

        public static Criterion<StartRidingTrigger.a> playerStartsRiding(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.START_RIDING_TRIGGER.createCriterion(new StartRidingTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
        }
    }
}
