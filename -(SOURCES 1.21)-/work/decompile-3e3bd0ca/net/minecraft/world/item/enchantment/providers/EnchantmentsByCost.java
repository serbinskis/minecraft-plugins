package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.WeightedRandomEnchant;

public record EnchantmentsByCost(HolderSet<Enchantment> enchantments, IntProvider cost) implements EnchantmentProvider {

    public static final MapCodec<EnchantmentsByCost> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCost::enchantments), IntProvider.CODEC.fieldOf("cost").forGetter(EnchantmentsByCost::cost)).apply(instance, EnchantmentsByCost::new);
    });

    @Override
    public void enchant(ItemStack itemstack, ItemEnchantments.a itemenchantments_a, RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        List<WeightedRandomEnchant> list = EnchantmentManager.selectEnchantment(randomsource, itemstack, this.cost.sample(randomsource), this.enchantments.stream());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) iterator.next();

            itemenchantments_a.upgrade(weightedrandomenchant.enchantment, weightedrandomenchant.level);
        }

    }

    @Override
    public MapCodec<EnchantmentsByCost> codec() {
        return EnchantmentsByCost.CODEC;
    }
}
