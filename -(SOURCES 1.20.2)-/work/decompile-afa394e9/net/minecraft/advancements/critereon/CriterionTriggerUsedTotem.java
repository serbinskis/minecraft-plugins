package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerUsedTotem extends CriterionTriggerAbstract<CriterionTriggerUsedTotem.a> {

    public CriterionTriggerUsedTotem() {}

    @Override
    public CriterionTriggerUsedTotem.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));

        return new CriterionTriggerUsedTotem.a(optional, optional1);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerusedtotem_a) -> {
            return criteriontriggerusedtotem_a.matches(itemstack);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1) {
            super(optional);
            this.item = optional1;
        }

        public static Criterion<CriterionTriggerUsedTotem.a> usedTotem(CriterionConditionItem criterionconditionitem) {
            return CriterionTriggers.USED_TOTEM.createCriterion(new CriterionTriggerUsedTotem.a(Optional.empty(), Optional.of(criterionconditionitem)));
        }

        public static Criterion<CriterionTriggerUsedTotem.a> usedTotem(IMaterial imaterial) {
            return CriterionTriggers.USED_TOTEM.createCriterion(new CriterionTriggerUsedTotem.a(Optional.empty(), Optional.of(CriterionConditionItem.a.item().of(imaterial).build())));
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
