package net.minecraft.world.item.enchantment;

public class EnchantmentTridentRiptide extends Enchantment {

    public EnchantmentTridentRiptide(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.LOYALTY && enchantment != Enchantments.CHANNELING;
    }
}
