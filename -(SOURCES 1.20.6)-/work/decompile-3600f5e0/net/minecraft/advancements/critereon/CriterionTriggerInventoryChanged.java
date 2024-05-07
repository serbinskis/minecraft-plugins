package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerInventoryChanged extends CriterionTriggerAbstract<CriterionTriggerInventoryChanged.a> {

    public CriterionTriggerInventoryChanged() {}

    @Override
    public Codec<CriterionTriggerInventoryChanged.a> codec() {
        return CriterionTriggerInventoryChanged.a.CODEC;
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

    public static record a(Optional<ContextAwarePredicate> player, CriterionTriggerInventoryChanged.a.a slots, List<CriterionConditionItem> items) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerInventoryChanged.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerInventoryChanged.a::player), CriterionTriggerInventoryChanged.a.a.CODEC.optionalFieldOf("slots", CriterionTriggerInventoryChanged.a.a.ANY).forGetter(CriterionTriggerInventoryChanged.a::slots), CriterionConditionItem.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(CriterionTriggerInventoryChanged.a::items)).apply(instance, CriterionTriggerInventoryChanged.a::new);
        });

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(CriterionConditionItem.a... acriterionconditionitem_a) {
            return hasItems((CriterionConditionItem[]) Stream.of(acriterionconditionitem_a).map(CriterionConditionItem.a::build).toArray((i) -> {
                return new CriterionConditionItem[i];
            }));
        }

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(CriterionConditionItem... acriterionconditionitem) {
            return CriterionTriggers.INVENTORY_CHANGED.createCriterion(new CriterionTriggerInventoryChanged.a(Optional.empty(), CriterionTriggerInventoryChanged.a.a.ANY, List.of(acriterionconditionitem)));
        }

        public static Criterion<CriterionTriggerInventoryChanged.a> hasItems(IMaterial... aimaterial) {
            CriterionConditionItem[] acriterionconditionitem = new CriterionConditionItem[aimaterial.length];

            for (int i = 0; i < aimaterial.length; ++i) {
                acriterionconditionitem[i] = new CriterionConditionItem(Optional.of(HolderSet.direct(aimaterial[i].asItem().builtInRegistryHolder())), CriterionConditionValue.IntegerRange.ANY, DataComponentPredicate.EMPTY, Map.of());
            }

            return hasItems(acriterionconditionitem);
        }

        public boolean matches(PlayerInventory playerinventory, ItemStack itemstack, int i, int j, int k) {
            if (!this.slots.matches(i, j, k)) {
                return false;
            } else if (this.items.isEmpty()) {
                return true;
            } else if (this.items.size() != 1) {
                List<CriterionConditionItem> list = new ObjectArrayList(this.items);
                int l = playerinventory.getContainerSize();

                for (int i1 = 0; i1 < l; ++i1) {
                    if (list.isEmpty()) {
                        return true;
                    }

                    ItemStack itemstack1 = playerinventory.getItem(i1);

                    if (!itemstack1.isEmpty()) {
                        list.removeIf((criterionconditionitem) -> {
                            return criterionconditionitem.test(itemstack1);
                        });
                    }
                }

                return list.isEmpty();
            } else {
                return !itemstack.isEmpty() && ((CriterionConditionItem) this.items.get(0)).test(itemstack);
            }
        }

        public static record a(CriterionConditionValue.IntegerRange occupied, CriterionConditionValue.IntegerRange full, CriterionConditionValue.IntegerRange empty) {

            public static final Codec<CriterionTriggerInventoryChanged.a.a> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("occupied", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerInventoryChanged.a.a::occupied), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("full", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerInventoryChanged.a.a::full), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("empty", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerInventoryChanged.a.a::empty)).apply(instance, CriterionTriggerInventoryChanged.a.a::new);
            });
            public static final CriterionTriggerInventoryChanged.a.a ANY = new CriterionTriggerInventoryChanged.a.a(CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY, CriterionConditionValue.IntegerRange.ANY);

            public boolean matches(int i, int j, int k) {
                return !this.full.matches(i) ? false : (!this.empty.matches(j) ? false : this.occupied.matches(k));
            }
        }
    }
}
