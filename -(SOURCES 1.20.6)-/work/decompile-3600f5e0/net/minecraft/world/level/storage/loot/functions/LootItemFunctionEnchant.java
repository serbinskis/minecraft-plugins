package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootItemFunctionEnchant extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<HolderSet<Enchantment>> ENCHANTMENT_SET_CODEC = BuiltInRegistries.ENCHANTMENT.holderByNameCodec().listOf().xmap(HolderSet::direct, (holderset) -> {
        return holderset.stream().toList();
    });
    public static final MapCodec<LootItemFunctionEnchant> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(LootItemFunctionEnchant.ENCHANTMENT_SET_CODEC.optionalFieldOf("enchantments").forGetter((lootitemfunctionenchant) -> {
            return lootitemfunctionenchant.enchantments;
        })).apply(instance, LootItemFunctionEnchant::new);
    });
    private final Optional<HolderSet<Enchantment>> enchantments;

    LootItemFunctionEnchant(List<LootItemCondition> list, Optional<HolderSet<Enchantment>> optional) {
        super(list);
        this.enchantments = optional;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionEnchant> getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        RandomSource randomsource = loottableinfo.getRandom();
        Optional<Holder<Enchantment>> optional = this.enchantments.flatMap((holderset) -> {
            return holderset.getRandomElement(randomsource);
        }).or(() -> {
            boolean flag = itemstack.is(Items.BOOK);
            List<Holder.c<Enchantment>> list = BuiltInRegistries.ENCHANTMENT.holders().filter((holder_c) -> {
                return ((Enchantment) holder_c.value()).isEnabled(loottableinfo.getLevel().enabledFeatures());
            }).filter((holder_c) -> {
                return ((Enchantment) holder_c.value()).isDiscoverable();
            }).filter((holder_c) -> {
                return flag || ((Enchantment) holder_c.value()).canEnchant(itemstack);
            }).toList();

            return SystemUtils.getRandomSafe(list, randomsource);
        });

        if (optional.isEmpty()) {
            LootItemFunctionEnchant.LOGGER.warn("Couldn't find a compatible enchantment for {}", itemstack);
            return itemstack;
        } else {
            return enchantItem(itemstack, (Enchantment) ((Holder) optional.get()).value(), randomsource);
        }
    }

    private static ItemStack enchantItem(ItemStack itemstack, Enchantment enchantment, RandomSource randomsource) {
        int i = MathHelper.nextInt(randomsource, enchantment.getMinLevel(), enchantment.getMaxLevel());

        if (itemstack.is(Items.BOOK)) {
            itemstack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        itemstack.enchant(enchantment, i);
        return itemstack;
    }

    public static LootItemFunctionEnchant.a randomEnchantment() {
        return new LootItemFunctionEnchant.a();
    }

    public static LootItemFunctionConditional.a<?> randomApplicableEnchantment() {
        return simpleBuilder((list) -> {
            return new LootItemFunctionEnchant(list, Optional.empty());
        });
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionEnchant.a> {

        private final List<Holder<Enchantment>> enchantments = new ArrayList();

        public a() {}

        @Override
        protected LootItemFunctionEnchant.a getThis() {
            return this;
        }

        public LootItemFunctionEnchant.a withEnchantment(Enchantment enchantment) {
            this.enchantments.add(enchantment.builtInRegistryHolder());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionEnchant(this.getConditions(), this.enchantments.isEmpty() ? Optional.empty() : Optional.of(HolderSet.direct(this.enchantments)));
        }
    }
}
