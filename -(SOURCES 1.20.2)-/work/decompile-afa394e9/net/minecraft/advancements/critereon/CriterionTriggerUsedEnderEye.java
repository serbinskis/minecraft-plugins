package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;

public class CriterionTriggerUsedEnderEye extends CriterionTriggerAbstract<CriterionTriggerUsedEnderEye.a> {

    public CriterionTriggerUsedEnderEye() {}

    @Override
    public CriterionTriggerUsedEnderEye.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange = CriterionConditionValue.DoubleRange.fromJson(jsonobject.get("distance"));

        return new CriterionTriggerUsedEnderEye.a(optional, criterionconditionvalue_doublerange);
    }

    public void trigger(EntityPlayer entityplayer, BlockPosition blockposition) {
        double d0 = entityplayer.getX() - (double) blockposition.getX();
        double d1 = entityplayer.getZ() - (double) blockposition.getZ();
        double d2 = d0 * d0 + d1 * d1;

        this.trigger(entityplayer, (criteriontriggerusedendereye_a) -> {
            return criteriontriggerusedendereye_a.matches(d2);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionValue.DoubleRange level;

        public a(Optional<ContextAwarePredicate> optional, CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            super(optional);
            this.level = criterionconditionvalue_doublerange;
        }

        public boolean matches(double d0) {
            return this.level.matchesSqr(d0);
        }
    }
}
