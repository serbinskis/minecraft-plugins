package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
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
        }), Codec.BOOL.fieldOf("treasure").orElse(false).forGetter((lootenchantlevel) -> {
            return lootenchantlevel.treasure;
        }))).apply(instance, LootEnchantLevel::new);
    });
    private final NumberProvider levels;
    private final boolean treasure;

    LootEnchantLevel(List<LootItemCondition> list, NumberProvider numberprovider, boolean flag) {
        super(list);
        this.levels = numberprovider;
        this.treasure = flag;
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

        return EnchantmentManager.enchantItem(loottableinfo.getLevel().enabledFeatures(), randomsource, itemstack, this.levels.getInt(loottableinfo), this.treasure);
    }

    public static LootEnchantLevel.a enchantWithLevels(NumberProvider numberprovider) {
        return new LootEnchantLevel.a(numberprovider);
    }

    public static class a extends LootItemFunctionConditional.a<LootEnchantLevel.a> {

        private final NumberProvider levels;
        private boolean treasure;

        public a(NumberProvider numberprovider) {
            this.levels = numberprovider;
        }

        @Override
        protected LootEnchantLevel.a getThis() {
            return this;
        }

        public LootEnchantLevel.a allowTreasure() {
            this.treasure = true;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootEnchantLevel(this.getConditions(), this.levels, this.treasure);
        }
    }
}
