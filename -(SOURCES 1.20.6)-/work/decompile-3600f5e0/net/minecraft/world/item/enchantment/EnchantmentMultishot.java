package net.minecraft.world.item.enchantment;

public class EnchantmentMultishot extends Enchantment {

    public EnchantmentMultishot(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.PIERCING;
    }
}
