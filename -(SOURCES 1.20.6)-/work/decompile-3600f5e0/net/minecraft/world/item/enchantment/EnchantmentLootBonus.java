package net.minecraft.world.item.enchantment;

public class EnchantmentLootBonus extends Enchantment {

    protected EnchantmentLootBonus(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.SILK_TOUCH;
    }
}
