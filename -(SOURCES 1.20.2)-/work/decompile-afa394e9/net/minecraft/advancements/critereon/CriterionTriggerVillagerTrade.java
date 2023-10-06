package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.npc.EntityVillagerAbstract;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerVillagerTrade extends CriterionTriggerAbstract<CriterionTriggerVillagerTrade.a> {

    public CriterionTriggerVillagerTrade() {}

    @Override
    public CriterionTriggerVillagerTrade.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "villager", lootdeserializationcontext);
        Optional<CriterionConditionItem> optional2 = CriterionConditionItem.fromJson(jsonobject.get("item"));

        return new CriterionTriggerVillagerTrade.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, EntityVillagerAbstract entityvillagerabstract, ItemStack itemstack) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityvillagerabstract);

        this.trigger(entityplayer, (criteriontriggervillagertrade_a) -> {
            return criteriontriggervillagertrade_a.matches(loottableinfo, itemstack);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> villager;
        private final Optional<CriterionConditionItem> item;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1, Optional<CriterionConditionItem> optional2) {
            super(optional);
            this.villager = optional1;
            this.item = optional2;
        }

        public static Criterion<CriterionTriggerVillagerTrade.a> tradedWithVillager() {
            return CriterionTriggers.TRADE.createCriterion(new CriterionTriggerVillagerTrade.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerVillagerTrade.a> tradedWithVillager(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.TRADE.createCriterion(new CriterionTriggerVillagerTrade.a(Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootTableInfo loottableinfo, ItemStack itemstack) {
            return this.villager.isPresent() && !((ContextAwarePredicate) this.villager.get()).matches(loottableinfo) ? false : !this.item.isPresent() || ((CriterionConditionItem) this.item.get()).matches(itemstack);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            this.villager.ifPresent((contextawarepredicate) -> {
                jsonobject.add("villager", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
