package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerEnchantedItem extends CriterionTriggerAbstract<CriterionTriggerEnchantedItem.a> {

    public CriterionTriggerEnchantedItem() {}

    @Override
    public CriterionTriggerEnchantedItem.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject.get("levels"));

        return new CriterionTriggerEnchantedItem.a(optional, optional1, criterionconditionvalue_integerrange);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggerenchanteditem_a) -> {
            return criteriontriggerenchanteditem_a.matches(itemstack, i);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;
        private final CriterionConditionValue.IntegerRange levels;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            super(optional);
            this.item = optional1;
            this.levels = criterionconditionvalue_integerrange;
        }

        public static Criterion<CriterionTriggerEnchantedItem.a> enchantedItem() {
            return CriterionTriggers.ENCHANTED_ITEM.createCriterion(new CriterionTriggerEnchantedItem.a(Optional.empty(), Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(ItemStack itemstack, int i) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : this.levels.matches(i);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            jsonobject.add("levels", this.levels.serializeToJson());
            return jsonobject;
        }
    }
}
