package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record SingleEnchantment(Holder<Enchantment> enchantment, IntProvider level) implements EnchantmentProvider {

    public static final MapCodec<SingleEnchantment> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Enchantment.CODEC.fieldOf("enchantment").forGetter(SingleEnchantment::enchantment), IntProvider.CODEC.fieldOf("level").forGetter(SingleEnchantment::level)).apply(instance, SingleEnchantment::new);
    });

    @Override
    public void enchant(ItemStack itemstack, ItemEnchantments.a itemenchantments_a, RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        itemenchantments_a.upgrade(this.enchantment, MathHelper.clamp(this.level.sample(randomsource), ((Enchantment) this.enchantment.value()).getMinLevel(), ((Enchantment) this.enchantment.value()).getMaxLevel()));
    }

    @Override
    public MapCodec<SingleEnchantment> codec() {
        return SingleEnchantment.CODEC;
    }
}
