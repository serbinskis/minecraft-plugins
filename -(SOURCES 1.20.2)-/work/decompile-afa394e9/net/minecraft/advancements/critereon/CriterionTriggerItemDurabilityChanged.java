package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerItemDurabilityChanged extends CriterionTriggerAbstract<CriterionTriggerItemDurabilityChanged.a> {

    public CriterionTriggerItemDurabilityChanged() {}

    @Override
    public CriterionTriggerItemDurabilityChanged.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("durability"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange1 = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("delta"));

        return new CriterionTriggerItemDurabilityChanged.a(optional, optional1, criterionconditionvalue_integerrange, criterionconditionvalue_integerrange1);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggeritemdurabilitychanged_a) -> {
            return criteriontriggeritemdurabilitychanged_a.matches(itemstack, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;
        private final CriterionConditionValue.IntegerRange durability;
        private final CriterionConditionValue.IntegerRange delta;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange1) {
            super(optional);
            this.item = optional1;
            this.durability = criterionconditionvalue_integerrange;
            this.delta = criterionconditionvalue_integerrange1;
        }

        public static Criterion<CriterionTriggerItemDurabilityChanged.a> changedDurability(Optional<CriterionConditionItem> optional, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return changedDurability(Optional.empty(), optional, criterionconditionvalue_integerrange);
        }

        public static Criterion<CriterionTriggerItemDurabilityChanged.a> changedDurability(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new CriterionTriggerItemDurabilityChanged.a(optional, optional1, criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(ItemStack itemstack, int i) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : (!this.durability.matches(itemstack.getMaxDamage() - i) ? false : this.delta.matches(itemstack.getDamageValue() - i));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            jsonobject.add("durability", this.durability.serializeToJson());
            jsonobject.add("delta", this.delta.serializeToJson());
            return jsonobject;
        }
    }
}
