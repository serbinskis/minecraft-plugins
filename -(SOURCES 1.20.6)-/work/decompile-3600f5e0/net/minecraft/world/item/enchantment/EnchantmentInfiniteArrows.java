package net.minecraft.world.item.enchantment;

public class EnchantmentInfiniteArrows extends Enchantment {

    public EnchantmentInfiniteArrows(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return enchantment instanceof EnchantmentMending ? false : super.checkCompatibility(enchantment);
    }
}
