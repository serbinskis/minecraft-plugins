package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public record CriterionConditionItem(Optional<HolderSet<Item>> items, CriterionConditionValue.IntegerRange count, DataComponentPredicate components, Map<ItemSubPredicate.a<?>, ItemSubPredicate> subPredicates) implements Predicate<ItemStack> {

    public static final Codec<CriterionConditionItem> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(CriterionConditionItem::items), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("count", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionItem::count), DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(CriterionConditionItem::components), ItemSubPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(CriterionConditionItem::subPredicates)).apply(instance, CriterionConditionItem::new);
    });

    public boolean test(ItemStack itemstack) {
        if (this.items.isPresent() && !itemstack.is((HolderSet) this.items.get())) {
            return false;
        } else if (!this.count.matches(itemstack.getCount())) {
            return false;
        } else if (!this.components.test((DataComponentHolder) itemstack)) {
            return false;
        } else {
            Iterator iterator = this.subPredicates.values().iterator();

            ItemSubPredicate itemsubpredicate;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                itemsubpredicate = (ItemSubPredicate) iterator.next();
            } while (itemsubpredicate.matches(itemstack));

            return false;
        }
    }

    public static class a {

        private Optional<HolderSet<Item>> items = Optional.empty();
        private CriterionConditionValue.IntegerRange count;
        private DataComponentPredicate components;
        private final Builder<ItemSubPredicate.a<?>, ItemSubPredicate> subPredicates;

        private a() {
            this.count = CriterionConditionValue.IntegerRange.ANY;
            this.components = DataComponentPredicate.EMPTY;
            this.subPredicates = ImmutableMap.builder();
        }

        public static CriterionConditionItem.a item() {
            return new CriterionConditionItem.a();
        }

        public CriterionConditionItem.a of(IMaterial... aimaterial) {
            this.items = Optional.of(HolderSet.direct((imaterial) -> {
                return imaterial.asItem().builtInRegistryHolder();
            }, (Object[]) aimaterial));
            return this;
        }

        public CriterionConditionItem.a of(TagKey<Item> tagkey) {
            this.items = Optional.of(BuiltInRegistries.ITEM.getOrCreateTag(tagkey));
            return this;
        }

        public CriterionConditionItem.a withCount(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.count = criterionconditionvalue_integerrange;
            return this;
        }

        public <T extends ItemSubPredicate> CriterionConditionItem.a withSubPredicate(ItemSubPredicate.a<T> itemsubpredicate_a, T t0) {
            this.subPredicates.put(itemsubpredicate_a, t0);
            return this;
        }

        public CriterionConditionItem.a hasComponents(DataComponentPredicate datacomponentpredicate) {
            this.components = datacomponentpredicate;
            return this;
        }

        public CriterionConditionItem build() {
            return new CriterionConditionItem(this.items, this.count, this.components, this.subPredicates.build());
        }
    }
}
