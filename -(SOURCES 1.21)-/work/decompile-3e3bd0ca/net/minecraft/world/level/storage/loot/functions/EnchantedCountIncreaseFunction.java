package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantedCountIncreaseFunction extends LootItemFunctionConditional {

    public static final int NO_LIMIT = 0;
    public static final MapCodec<EnchantedCountIncreaseFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(Enchantment.CODEC.fieldOf("enchantment").forGetter((enchantedcountincreasefunction) -> {
            return enchantedcountincreasefunction.enchantment;
        }), NumberProviders.CODEC.fieldOf("count").forGetter((enchantedcountincreasefunction) -> {
            return enchantedcountincreasefunction.value;
        }), Codec.INT.optionalFieldOf("limit", 0).forGetter((enchantedcountincreasefunction) -> {
            return enchantedcountincreasefunction.limit;
        }))).apply(instance, EnchantedCountIncreaseFunction::new);
    });
    private final Holder<Enchantment> enchantment;
    private final NumberProvider value;
    private final int limit;

    EnchantedCountIncreaseFunction(List<LootItemCondition> list, Holder<Enchantment> holder, NumberProvider numberprovider, int i) {
        super(list);
        this.enchantment = holder;
        this.value = numberprovider;
        this.limit = i;
    }

    @Override
    public LootItemFunctionType<EnchantedCountIncreaseFunction> getType() {
        return LootItemFunctions.ENCHANTED_COUNT_INCREASE;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(LootContextParameters.ATTACKING_ENTITY), this.value.getReferencedContextParams());
    }

    private boolean hasLimit() {
        return this.limit > 0;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.ATTACKING_ENTITY);

        if (entity instanceof EntityLiving entityliving) {
            int i = EnchantmentManager.getEnchantmentLevel(this.enchantment, entityliving);

            if (i == 0) {
                return itemstack;
            }

            float f = (float) i * this.value.getFloat(loottableinfo);

            itemstack.grow(Math.round(f));
            if (this.hasLimit()) {
                itemstack.limitSize(this.limit);
            }
        }

        return itemstack;
    }

    public static EnchantedCountIncreaseFunction.a lootingMultiplier(HolderLookup.a holderlookup_a, NumberProvider numberprovider) {
        HolderLookup.b<Enchantment> holderlookup_b = holderlookup_a.lookupOrThrow(Registries.ENCHANTMENT);

        return new EnchantedCountIncreaseFunction.a(holderlookup_b.getOrThrow(Enchantments.LOOTING), numberprovider);
    }

    public static class a extends LootItemFunctionConditional.a<EnchantedCountIncreaseFunction.a> {

        private final Holder<Enchantment> enchantment;
        private final NumberProvider count;
        private int limit = 0;

        public a(Holder<Enchantment> holder, NumberProvider numberprovider) {
            this.enchantment = holder;
            this.count = numberprovider;
        }

        @Override
        protected EnchantedCountIncreaseFunction.a getThis() {
            return this;
        }

        public EnchantedCountIncreaseFunction.a setLimit(int i) {
            this.limit = i;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantedCountIncreaseFunction(this.getConditions(), this.enchantment, this.count, this.limit);
        }
    }
}
