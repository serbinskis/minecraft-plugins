package net.minecraft.world.item.enchantment;

public class EnchantmentPiercing extends Enchantment {

    public EnchantmentPiercing(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.MULTISHOT;
    }
}
