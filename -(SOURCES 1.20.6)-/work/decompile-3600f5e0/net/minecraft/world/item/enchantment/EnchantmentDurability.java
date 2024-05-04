package net.minecraft.world.item.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemArmor;
import net.minecraft.world.item.ItemStack;

public class EnchantmentDurability extends Enchantment {

    protected EnchantmentDurability(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    public static boolean shouldIgnoreDurabilityDrop(ItemStack itemstack, int i, RandomSource randomsource) {
        return itemstack.getItem() instanceof ItemArmor && randomsource.nextFloat() < 0.6F ? false : randomsource.nextInt(i + 1) > 0;
    }
}
