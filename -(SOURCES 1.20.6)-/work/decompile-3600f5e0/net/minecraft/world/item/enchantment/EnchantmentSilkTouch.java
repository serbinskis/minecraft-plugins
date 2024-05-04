package net.minecraft.world.item.enchantment;

public class EnchantmentSilkTouch extends Enchantment {

    protected EnchantmentSilkTouch(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FORTUNE;
    }
}
