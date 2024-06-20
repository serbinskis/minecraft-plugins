package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootEnchantLevel extends LootItemFunctionConditional {

    public static final MapCodec<LootEnchantLevel> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(NumberProviders.CODEC.fieldOf("levels").forGetter((lootenchantlevel) -> {
            return lootenchantlevel.levels;
        }), RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter((lootenchantlevel) -> {
            return lootenchantlevel.options;
        }))).apply(instance, LootEnchantLevel::new);
    });
    private final NumberProvider levels;
    private final Optional<HolderSet<Enchantment>> options;

    LootEnchantLevel(List<LootItemCondition> list, NumberProvider numberprovider, Optional<HolderSet<Enchantment>> optional) {
        super(list);
        this.levels = numberprovider;
        this.options = optional;
    }

    @Override
    public LootItemFunctionType<LootEnchantLevel> getType() {
        return LootItemFunctions.ENCHANT_WITH_LEVELS;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.levels.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        RandomSource randomsource = loottableinfo.getRandom();
        IRegistryCustom iregistrycustom = loottableinfo.getLevel().registryAccess();

        return EnchantmentManager.enchantItem(randomsource, itemstack, this.levels.getInt(loottableinfo), iregistrycustom, this.options);
    }

    public static LootEnchantLevel.a enchantWithLevels(HolderLookup.a holderlookup_a, NumberProvider numberprovider) {
        return (new LootEnchantLevel.a(numberprovider)).fromOptions(holderlookup_a.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
    }

    public static class a extends LootItemFunctionConditional.a<LootEnchantLevel.a> {

        private final NumberProvider levels;
        private Optional<HolderSet<Enchantment>> options = Optional.empty();

        public a(NumberProvider numberprovider) {
            this.levels = numberprovider;
        }

        @Override
        protected LootEnchantLevel.a getThis() {
            return this;
        }

        public LootEnchantLevel.a fromOptions(HolderSet<Enchantment> holderset) {
            this.options = Optional.of(holderset);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootEnchantLevel(this.getConditions(), this.levels, this.options);
        }
    }
}
