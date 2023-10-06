package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerPlayerInteractedWithEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerInteractedWithEntity.a> {

    public CriterionTriggerPlayerInteractedWithEntity() {}

    @Override
    protected CriterionTriggerPlayerInteractedWithEntity.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new CriterionTriggerPlayerInteractedWithEntity.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerinteractedwithentity_a) -> {
            return criteriontriggerplayerinteractedwithentity_a.matches(itemstack, loottableinfo);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> item;
        private final Optional<ContextAwarePredicate> entity;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, Optional<ContextAwarePredicate> optional2) {
            super(optional);
            this.item = optional1;
            this.entity = optional2;
        }

        public static Criterion<CriterionTriggerPlayerInteractedWithEntity.a> itemUsedOnEntity(Optional<ContextAwarePredicate> optional, CriterionConditionItem.a criterionconditionitem_a, Optional<ContextAwarePredicate> optional1) {
            return CriterionTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new CriterionTriggerPlayerInteractedWithEntity.a(optional, Optional.of(criterionconditionitem_a.build()), optional1));
        }

        public static Criterion<CriterionTriggerPlayerInteractedWithEntity.a> itemUsedOnEntity(CriterionConditionItem.a criterionconditionitem_a, Optional<ContextAwarePredicate> optional) {
            return itemUsedOnEntity(Optional.empty(), criterionconditionitem_a, optional);
        }

        public boolean matches(ItemStack itemstack, LootTableInfo loottableinfo) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : this.entity.isEmpty() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            this.entity.ifPresent((contextawarepredicate) -> {
                jsonobject.add("entity", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
