package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.WeightedRandomEnchant;

public record EnchantmentsByCostWithDifficulty(HolderSet<Enchantment> enchantments, int minCost, int maxCostSpan) implements EnchantmentProvider {

    public static final int MAX_ALLOWED_VALUE_PART = 10000;
    public static final MapCodec<EnchantmentsByCostWithDifficulty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCostWithDifficulty::enchantments), ExtraCodecs.intRange(1, 10000).fieldOf("min_cost").forGetter(EnchantmentsByCostWithDifficulty::minCost), ExtraCodecs.intRange(0, 10000).fieldOf("max_cost_span").forGetter(EnchantmentsByCostWithDifficulty::maxCostSpan)).apply(instance, EnchantmentsByCostWithDifficulty::new);
    });

    @Override
    public void enchant(ItemStack itemstack, ItemEnchantments.a itemenchantments_a, RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        float f = difficultydamagescaler.getSpecialMultiplier();
        int i = MathHelper.randomBetweenInclusive(randomsource, this.minCost, this.minCost + (int) (f * (float) this.maxCostSpan));
        List<WeightedRandomEnchant> list = EnchantmentManager.selectEnchantment(randomsource, itemstack, i, this.enchantments.stream());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) iterator.next();

            itemenchantments_a.upgrade(weightedrandomenchant.enchantment, weightedrandomenchant.level);
        }

    }

    @Override
    public MapCodec<EnchantmentsByCostWithDifficulty> codec() {
        return EnchantmentsByCostWithDifficulty.CODEC;
    }
}
