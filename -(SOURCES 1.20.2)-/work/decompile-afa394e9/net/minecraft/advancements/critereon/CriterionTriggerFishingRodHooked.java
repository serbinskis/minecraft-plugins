package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class CriterionTriggerFishingRodHooked extends CriterionTriggerAbstract<CriterionTriggerFishingRodHooked.a> {

    public CriterionTriggerFishingRodHooked() {}

    @Override
    public CriterionTriggerFishingRodHooked.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<CriterionConditionItem> optional1 = CriterionConditionItem.fromJson(jsonobject.get("rod"));
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "entity", lootdeserializationcontext);
        Optional<CriterionConditionItem> optional3 = CriterionConditionItem.fromJson(jsonobject.get("item"));

        return new CriterionTriggerFishingRodHooked.a(optional, optional1, optional2, optional3);
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, EntityFishingHook entityfishinghook, Collection<ItemStack> collection) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, (Entity) (entityfishinghook.getHookedIn() != null ? entityfishinghook.getHookedIn() : entityfishinghook));

        this.trigger(entityplayer, (criteriontriggerfishingrodhooked_a) -> {
            return criteriontriggerfishingrodhooked_a.matches(itemstack, loottableinfo, collection);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<CriterionConditionItem> rod;
        private final Optional<ContextAwarePredicate> entity;
        private final Optional<CriterionConditionItem> item;

        public a(Optional<ContextAwarePredicate> optional, Optional<CriterionConditionItem> optional1, Optional<ContextAwarePredicate> optional2, Optional<CriterionConditionItem> optional3) {
            super(optional);
            this.rod = optional1;
            this.entity = optional2;
            this.item = optional3;
        }

        public static Criterion<CriterionTriggerFishingRodHooked.a> fishedItem(Optional<CriterionConditionItem> optional, Optional<CriterionConditionEntity> optional1, Optional<CriterionConditionItem> optional2) {
            return CriterionTriggers.FISHING_ROD_HOOKED.createCriterion(new CriterionTriggerFishingRodHooked.a(Optional.empty(), optional, CriterionConditionEntity.wrap(optional1), optional2));
        }

        public boolean matches(ItemStack itemstack, LootTableInfo loottableinfo, Collection<ItemStack> collection) {
            if (this.rod.isPresent() && !((CriterionConditionItem) this.rod.get()).matches(itemstack)) {
                return false;
            } else if (this.entity.isPresent() && !((ContextAwarePredicate) this.entity.get()).matches(loottableinfo)) {
                return false;
            } else {
                if (this.item.isPresent()) {
                    boolean flag = false;
                    Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.THIS_ENTITY);

                    if (entity instanceof EntityItem) {
                        EntityItem entityitem = (EntityItem) entity;

                        if (((CriterionConditionItem) this.item.get()).matches(entityitem.getItem())) {
                            flag = true;
                        }
                    }

                    Iterator iterator = collection.iterator();

                    while (iterator.hasNext()) {
                        ItemStack itemstack1 = (ItemStack) iterator.next();

                        if (((CriterionConditionItem) this.item.get()).matches(itemstack1)) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.rod.ifPresent((criterionconditionitem) -> {
                jsonobject.add("rod", criterionconditionitem.serializeToJson());
            });
            this.entity.ifPresent((contextawarepredicate) -> {
                jsonobject.add("entity", contextawarepredicate.toJson());
            });
            this.item.ifPresent((criterionconditionitem) -> {
                jsonobject.add("item", criterionconditionitem.serializeToJson());
            });
            return jsonobject;
        }
    }
}
