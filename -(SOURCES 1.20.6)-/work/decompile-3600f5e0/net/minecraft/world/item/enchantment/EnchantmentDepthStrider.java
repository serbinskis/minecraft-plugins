package net.minecraft.world.item.enchantment;

public class EnchantmentDepthStrider extends Enchantment {

    public EnchantmentDepthStrider(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
    }
}
