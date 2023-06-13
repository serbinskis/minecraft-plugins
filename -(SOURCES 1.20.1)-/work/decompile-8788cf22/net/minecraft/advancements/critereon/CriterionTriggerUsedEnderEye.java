package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPosition;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;

public class CriterionTriggerUsedEnderEye extends CriterionTriggerAbstract<CriterionTriggerUsedEnderEye.a> {

    static final MinecraftKey ID = new MinecraftKey("used_ender_eye");

    public CriterionTriggerUsedEnderEye() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerUsedEnderEye.ID;
    }

    @Override
    public CriterionTriggerUsedEnderEye.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange = CriterionConditionValue.DoubleRange.fromJson(jsonobject.get("distance"));

        return new CriterionTriggerUsedEnderEye.a(contextawarepredicate, criterionconditionvalue_doublerange);
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

        public a(ContextAwarePredicate contextawarepredicate, CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            super(CriterionTriggerUsedEnderEye.ID, contextawarepredicate);
            this.level = criterionconditionvalue_doublerange;
        }

        public boolean matches(double d0) {
            return this.level.matchesSqr(d0);
        }
    }
}
