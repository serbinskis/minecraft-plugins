package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootEnchantFunction extends LootItemFunctionConditional {

    public static final int NO_LIMIT = 0;
    public static final MapCodec<LootEnchantFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(NumberProviders.CODEC.fieldOf("count").forGetter((lootenchantfunction) -> {
            return lootenchantfunction.value;
        }), Codec.INT.optionalFieldOf("limit", 0).forGetter((lootenchantfunction) -> {
            return lootenchantfunction.limit;
        }))).apply(instance, LootEnchantFunction::new);
    });
    private final NumberProvider value;
    private final int limit;

    LootEnchantFunction(List<LootItemCondition> list, NumberProvider numberprovider, int i) {
        super(list);
        this.value = numberprovider;
        this.limit = i;
    }

    @Override
    public LootItemFunctionType<LootEnchantFunction> getType() {
        return LootItemFunctions.LOOTING_ENCHANT;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(LootContextParameters.KILLER_ENTITY), this.value.getReferencedContextParams());
    }

    private boolean hasLimit() {
        return this.limit > 0;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getParamOrNull(LootContextParameters.KILLER_ENTITY);

        if (entity instanceof EntityLiving) {
            int i = EnchantmentManager.getMobLooting((EntityLiving) entity);

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

    public static LootEnchantFunction.a lootingMultiplier(NumberProvider numberprovider) {
        return new LootEnchantFunction.a(numberprovider);
    }

    public static class a extends LootItemFunctionConditional.a<LootEnchantFunction.a> {

        private final NumberProvider count;
        private int limit = 0;

        public a(NumberProvider numberprovider) {
            this.count = numberprovider;
        }

        @Override
        protected LootEnchantFunction.a getThis() {
            return this;
        }

        public LootEnchantFunction.a setLimit(int i) {
            this.limit = i;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootEnchantFunction(this.getConditions(), this.count, this.limit);
        }
    }
}
