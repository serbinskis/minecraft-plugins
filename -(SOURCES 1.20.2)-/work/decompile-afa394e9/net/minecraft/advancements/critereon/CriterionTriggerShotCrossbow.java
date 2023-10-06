package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerShotCrossbow extends CriterionTriggerAbstract<CriterionTriggerShotCrossbow.a> {

    public CriterionTriggerShotCrossbow() {}

    @Override
    public CriterionTriggerShotCrossbow.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));

        return new CriterionTriggerShotCrossbow.a(optional, optional1);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggershotcrossbow_a) -> {
            return criteriontriggershotcrossbow_a.matches(itemstack);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1) {
            super(optional);
            this.item = optional1;
        }

        public static Criterion<CriterionTriggerShotCrossbow.a> shotCrossbow(Optional<CriterionConditionItem> optional) {
            return CriterionTriggers.SHOT_CROSSBOW.createCriterion(new CriterionTriggerShotCrossbow.a(Optional.empty(), optional));
        }

        public static Criterion<CriterionTriggerShotCrossbow.a> shotCrossbow(IMaterial imaterial) {
            return CriterionTriggers.SHOT_CROSSBOW.createCriterion(new CriterionTriggerShotCrossbow.a(Optional.empty(), Optional.of(CriterionConditionItem.a.item().of(imaterial).build())));
        }

        public boolean matches(ItemStack itemstack) {
            return this.item.isEmpty() || ((CriterionConditionItem) this.item.get()).matches(itemstack);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            return jsonobject;
        }
    }
}
