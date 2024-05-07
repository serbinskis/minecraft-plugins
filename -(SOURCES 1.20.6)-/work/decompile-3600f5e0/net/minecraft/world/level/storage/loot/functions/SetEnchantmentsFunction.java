package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(Codec.unboundedMap(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), NumberProviders.CODEC).optionalFieldOf("enchantments", Map.of()).forGetter((setenchantmentsfunction) -> {
            return setenchantmentsfunction.enchantments;
        }), Codec.BOOL.fieldOf("add").orElse(false).forGetter((setenchantmentsfunction) -> {
            return setenchantmentsfunction.add;
        }))).apply(instance, SetEnchantmentsFunction::new);
    });
    private final Map<Holder<Enchantment>, NumberProvider> enchantments;
    private final boolean add;

    SetEnchantmentsFunction(List<LootItemCondition> list, Map<Holder<Enchantment>, NumberProvider> map, boolean flag) {
        super(list);
        this.enchantments = Map.copyOf(map);
        this.add = flag;
    }

    @Override
    public LootItemFunctionType<SetEnchantmentsFunction> getType() {
        return LootItemFunctions.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return (Set) this.enchantments.values().stream().flatMap((numberprovider) -> {
            return numberprovider.getReferencedContextParams().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Object2IntMap<Enchantment> object2intmap = new Object2IntOpenHashMap();

        this.enchantments.forEach((holder, numberprovider) -> {
            object2intmap.put((Enchantment) holder.value(), MathHelper.clamp(numberprovider.getInt(loottableinfo), 0, 255));
        });
        if (itemstack.is(Items.BOOK)) {
            itemstack = itemstack.transmuteCopy(Items.ENCHANTED_BOOK, itemstack.getCount());
            itemstack.set(DataComponents.STORED_ENCHANTMENTS, (ItemEnchantments) itemstack.remove(DataComponents.ENCHANTMENTS));
        }

        EnchantmentManager.updateEnchantments(itemstack, (itemenchantments_a) -> {
            if (this.add) {
                object2intmap.forEach((enchantment, integer) -> {
                    itemenchantments_a.set(enchantment, itemenchantments_a.getLevel(enchantment) + integer);
                });
            } else {
                Objects.requireNonNull(itemenchantments_a);
                object2intmap.forEach(itemenchantments_a::set);
            }

        });
        return itemstack;
    }

    public static class a extends LootItemFunctionConditional.a<SetEnchantmentsFunction.a> {

        private final Builder<Holder<Enchantment>, NumberProvider> enchantments;
        private final boolean add;

        public a() {
            this(false);
        }

        public a(boolean flag) {
            this.enchantments = ImmutableMap.builder();
            this.add = flag;
        }

        @Override
        protected SetEnchantmentsFunction.a getThis() {
            return this;
        }

        public SetEnchantmentsFunction.a withEnchantment(Enchantment enchantment, NumberProvider numberprovider) {
            this.enchantments.put(enchantment.builtInRegistryHolder(), numberprovider);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetEnchantmentsFunction(this.getConditions(), this.enchantments.build(), this.add);
        }
    }
}
