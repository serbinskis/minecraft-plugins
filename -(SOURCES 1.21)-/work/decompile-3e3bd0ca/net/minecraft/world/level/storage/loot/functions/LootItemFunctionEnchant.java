package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
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
    public static final MapCodec<LootItemFunctionEnchant> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter((lootitemfunctionenchant) -> {
            return lootitemfunctionenchant.options;
        }), Codec.BOOL.optionalFieldOf("only_compatible", true).forGetter((lootitemfunctionenchant) -> {
            return lootitemfunctionenchant.onlyCompatible;
        }))).apply(instance, LootItemFunctionEnchant::new);
    });
    private final Optional<HolderSet<Enchantment>> options;
    private final boolean onlyCompatible;

    LootItemFunctionEnchant(List<LootItemCondition> list, Optional<HolderSet<Enchantment>> optional, boolean flag) {
        super(list);
        this.options = optional;
        this.onlyCompatible = flag;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionEnchant> getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        RandomSource randomsource = loottableinfo.getRandom();
        boolean flag = itemstack.is(Items.BOOK);
        boolean flag1 = !flag && this.onlyCompatible;
        Stream<Holder<Enchantment>> stream = ((Stream) this.options.map(HolderSet::stream).orElseGet(() -> {
            return loottableinfo.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(Function.identity());
        })).filter((holder) -> {
            return !flag1 || ((Enchantment) holder.value()).canEnchant(itemstack);
        });
        List<Holder<Enchantment>> list = stream.toList();
        Optional<Holder<Enchantment>> optional = SystemUtils.getRandomSafe(list, randomsource);

        if (optional.isEmpty()) {
            LootItemFunctionEnchant.LOGGER.warn("Couldn't find a compatible enchantment for {}", itemstack);
            return itemstack;
        } else {
            return enchantItem(itemstack, (Holder) optional.get(), randomsource);
        }
    }

    private static ItemStack enchantItem(ItemStack itemstack, Holder<Enchantment> holder, RandomSource randomsource) {
        int i = MathHelper.nextInt(randomsource, ((Enchantment) holder.value()).getMinLevel(), ((Enchantment) holder.value()).getMaxLevel());

        if (itemstack.is(Items.BOOK)) {
            itemstack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        itemstack.enchant(holder, i);
        return itemstack;
    }

    public static LootItemFunctionEnchant.a randomEnchantment() {
        return new LootItemFunctionEnchant.a();
    }

    public static LootItemFunctionEnchant.a randomApplicableEnchantment(HolderLookup.a holderlookup_a) {
        return randomEnchantment().withOneOf(holderlookup_a.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionEnchant.a> {

        private Optional<HolderSet<Enchantment>> options = Optional.empty();
        private boolean onlyCompatible = true;

        public a() {}

        @Override
        protected LootItemFunctionEnchant.a getThis() {
            return this;
        }

        public LootItemFunctionEnchant.a withEnchantment(Holder<Enchantment> holder) {
            this.options = Optional.of(HolderSet.direct(holder));
            return this;
        }

        public LootItemFunctionEnchant.a withOneOf(HolderSet<Enchantment> holderset) {
            this.options = Optional.of(holderset);
            return this;
        }

        public LootItemFunctionEnchant.a allowingIncompatibleEnchantments() {
            this.onlyCompatible = false;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionEnchant(this.getConditions(), this.options, this.onlyCompatible);
        }
    }
}
