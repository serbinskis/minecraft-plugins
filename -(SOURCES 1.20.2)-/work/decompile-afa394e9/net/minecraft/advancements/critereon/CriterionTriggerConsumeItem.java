package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerConsumeItem extends CriterionTriggerAbstract<CriterionTriggerConsumeItem.a> {

    public CriterionTriggerConsumeItem() {}

    @Override
    public CriterionTriggerConsumeItem.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        return new CriterionTriggerConsumeItem.a(optional, CriterionConditionItem.fromJson(jsonobject.get("item")));
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerconsumeitem_a) -> {
            return criteriontriggerconsumeitem_a.matches(itemstack);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1) {
            super(optional);
            this.item = optional1;
        }

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem() {
            return CriterionTriggers.CONSUME_ITEM.createCriterion(new CriterionTriggerConsumeItem.a(Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem(IMaterial imaterial) {
            return usedItem(CriterionConditionItem.a.item().of(imaterial.asItem()));
        }

        public static Criterion<CriterionTriggerConsumeItem.a> usedItem(CriterionConditionItem.a criterionconditionitem_a) {
            return CriterionTriggers.CONSUME_ITEM.createCriterion(new CriterionTriggerConsumeItem.a(Optional.empty(), Optional.of(criterionconditionitem_a.build())));
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
