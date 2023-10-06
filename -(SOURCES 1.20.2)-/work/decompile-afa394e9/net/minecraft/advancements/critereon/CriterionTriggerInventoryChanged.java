package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerInventoryChanged extends CriterionTriggerAbstract<CriterionTriggerInventoryChanged.a> {

    public CriterionTriggerInventoryChanged() {}

    @Override
    public CriterionTriggerInventoryChanged.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "slots", new JsonObject());
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange = CriterionConditionValue.IntegerRange.fromJson(jsonobject1.get("occupied"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange1 = CriterionConditionValue.IntegerRange.fromJson(jsonobject1.get("full"));
        CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange2 = CriterionConditionValue.IntegerRange.fromJson(jsonobject1.get("empty"));
        List<CriterionConditionItem> list = CriterionConditionItem.fromJsonArray(jsonobject.get("items"));

        return new CriterionTriggerInventoryChanged.a(optional, criterionconditionvalue_integerrange, criterionconditionvalue_integerrange1, criterionconditionvalue_integerrange2, list);
    }

    public void trigger(EntityPlayer entityplayer, PlayerInventory playerinventory, ItemStack itemstack) {
        int i = 0;
        int j = 0;
        int k = 0;

        for (int l = 0; l < playerinventory.getContainerSize(); ++l) {
            ItemStack itemstack1 = playerinventory.getItem(l);

            if (itemstack1.isEmpty()) {
                ++j;
            } else {
                ++k;
                if (itemstack1.getCount() >= itemstack1.getMaxStackSize()) {
                    ++i;
                }
            }
        }

        this.trigger(entityplayer, playerinventory, itemstack, i, j, k);
    }

    private void trigger(EntityPlayer entityplayer, PlayerInventory playerinventory, ItemStack itemstack, int i, int j, int k) {
        this.trigger(entityplayer, (criteriontriggerinventorychanged_a) -> {
            return criteriontriggerinventorychanged_a.matches(playerinventory, itemstack, i, j, k);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final CriterionConditionValue.IntegerRange slotsOccupied;
        private final CriterionConditionValue.IntegerRange slotsFull;
        private final CriterionConditionValue.IntegerRange slotsEmpty;
        private final List<CriterionConditionItem> predicates;

        public a(Optional<ContextAwarePredicate> optional, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange1, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange2, List<CriterionConditionItem> list) {
            super(optional);
            this.slotsOccupied = criterionconditionvalue_integerrange;
            this.slotsFull = criterionconditionvalue_integerrange1;
            this.slotsEmpty = criterionconditionvalue_integerrange2;
            this.predicates = list;
        }

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(CriterionConditionItem.a... acriterionconditionitem_a) {
            return hasItems((CriterionConditionItem[]) Stream.of(acriterionconditionitem_a).map(CriterionConditionItem.a::build).toArray((i) -> {
                return new CriterionConditionItem[i];
            }));
        }

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(CriterionConditionItem... acriterionconditionitem) {
            return CriterionTriggers.INVENTORY_CHANGED.createCriterion(new CriterionTriggerInventoryChanged.a(Optional.empty(), CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY, List.of(acriterionconditionitem)));
        }

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(IMaterial... aimaterial) {
            CriterionConditionItem[] acriterionconditionitem = new CriterionConditionItem[aimaterial.length];

            for (int i = 0; i < aimaterial.length; ++i) {
                acriterionconditionitem[i] = new CriterionConditionItem(Optional.empty(), Optional.of(HolderSet.direct(aimaterial[i].asItem().builtInRegistryHolder())), CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY, List.of(), List.of(), Optional.empty(), Optional.empty());
            }

            return hasItems(acriterionconditionitem);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
                JsonObject jsonobject1 = new JsonObject();

                jsonobject1.add("occupied", this.slotsOccupied.serializeToJson());
                jsonobject1.add("full", this.slotsFull.serializeToJson());
                jsonobject1.add("empty", this.slotsEmpty.serializeToJson());
                jsonobject.add("slots", jsonobject1);
            }

            if (!this.predicates.isEmpty()) {
                jsonobject.add("items", CriterionConditionItem.serializeToJsonArray(this.predicates));
            }

            return jsonobject;
        }

        public boolean matches(PlayerInventory playerinventory, ItemStack itemstack, int i, int j, int k) {
            if (!this.slotsFull.matches(i)) {
                return false;
            } else if (!this.slotsEmpty.matches(j)) {
                return false;
            } else if (!this.slotsOccupied.matches(k)) {
                return false;
            } else if (this.predicates.isEmpty()) {
                return true;
            } else if (this.predicates.size() != 1) {
                List<CriterionConditionItem> list = new ObjectArrayList(this.predicates);
                int l = playerinventory.getContainerSize();

                for (int i1 = 0; i1 < l; ++i1) {
                    if (list.isEmpty()) {
                        return true;
                    }

                    ItemStack itemstack1 = playerinventory.getItem(i1);

                    if (!itemstack1.isEmpty()) {
                        list.removeIf((criterionconditionitem) -> {
                            return criterionconditionitem.matches(itemstack1);
                        });
                    }
                }

                return list.isEmpty();
            } else {
                return !itemstack.isEmpty() && ((CriterionConditionItem) this.predicates.get(0)).matches(itemstack);
            }
        }
    }
}
