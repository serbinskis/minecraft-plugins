package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemEnchantedBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.WeightedRandomEnchant;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemFunctionConditional {

    public static final Codec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(ExtraCodecs.strictOptionalField(Codec.unboundedMap(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), NumberProviders.CODEC), "enchantments", Map.of()).forGetter((setenchantmentsfunction) -> {
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
    public LootItemFunctionType getType() {
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
            object2intmap.put((Enchantment) holder.value(), numberprovider.getInt(loottableinfo));
        });
        if (itemstack.getItem() == Items.BOOK) {
            ItemStack itemstack1 = new ItemStack(Items.ENCHANTED_BOOK);

            object2intmap.forEach((enchantment, integer) -> {
                ItemEnchantedBook.addEnchantment(itemstack1, new WeightedRandomEnchant(enchantment, integer));
            });
            return itemstack1;
        } else {
            Map<Enchantment, Integer> map = EnchantmentManager.getEnchantments(itemstack);

            if (this.add) {
                object2intmap.forEach((enchantment, integer) -> {
                    updateEnchantment(map, enchantment, Math.max((Integer) map.getOrDefault(enchantment, 0) + integer, 0));
                });
            } else {
                object2intmap.forEach((enchantment, integer) -> {
                    updateEnchantment(map, enchantment, Math.max(integer, 0));
                });
            }

            EnchantmentManager.setEnchantments(map, itemstack);
            return itemstack;
        }
    }

    private static void updateEnchantment(Map<Enchantment, Integer> map, Enchantment enchantment, int i) {
        if (i == 0) {
            map.remove(enchantment);
        } else {
            map.put(enchantment, i);
        }

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
