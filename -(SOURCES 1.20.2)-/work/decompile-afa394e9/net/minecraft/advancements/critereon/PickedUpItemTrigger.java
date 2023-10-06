package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class PickedUpItemTrigger extends CriterionTriggerAbstract<PickedUpItemTrigger.a> {

    public PickedUpItemTrigger() {}

    @Override
    protected PickedUpItemTrigger.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("item"));
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);

        return new PickedUpItemTrigger.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, @Nullable Entity entity) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (pickedupitemtrigger_a) -> {
            return pickedupitemtrigger_a.matches(entityplayer, itemstack, loottableinfo);
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

        public static Criterion<PickedUpItemTrigger.a> thrownItemPickedUpByEntity(ContextAwarePredicate contextawarepredicate, Optional<CriterionConditionItem> optional, Optional<ContextAwarePredicate> optional1) {
            return CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.a(Optional.of(contextawarepredicate), optional, optional1));
        }

        public static Criterion<PickedUpItemTrigger.a> thrownItemPickedUpByPlayer(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, Optional<ContextAwarePredicate> optional2) {
            return CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.createCriterion(new PickedUpItemTrigger.a(optional, optional1, optional2));
        }

        public boolean matches(EntityPlayer entityplayer, ItemStack itemstack, LootTableInfo loottableinfo) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : !this.entity.isPresent() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
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
