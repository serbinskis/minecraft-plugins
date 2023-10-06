package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;

public class StartRidingTrigger extends CriterionTriggerAbstract<StartRidingTrigger.a> {

    public StartRidingTrigger() {}

    @Override
    public StartRidingTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        return new StartRidingTrigger.a(optional);
    }

    public void trigger(EntityPlayer entityplayer) {
        this.trigger(entityplayer, (startridingtrigger_a) -> {
            return true;
        });
    }

    public static class a extends CriterionInstanceAbstract {

        public a(Optional<ContextAwarePredicate> optional) {
            super(optional);
        }

        public static Criterion<StartRidingTrigger.a> playerStartsRiding(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.START_RIDING_TRIGGER.createCriterion(new StartRidingTrigger.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
        }
    }
}
